package com.backend.july.product.application;

import static com.backend.july.product.fixture.ProductFixture.productWithId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import com.backend.july.product.fixture.ProductFixture;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import com.backend.july.product.presentation.dto.response.CreateProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private CreateProductService createProductService;

    @Nested
    @DisplayName("상품 생성 성공")
    class CreateSuccess {

        @Test
        @DisplayName("상품과 초기 재고를 생성하고, 생성된 상품 정보를 반환한다.")
        void create_product_success() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest();

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            given(productRepository.save(any(Product.class)))
                    .willReturn(savedProduct);

            // when
            CreateProductResponse response = createProductService.create(request);

            // then
            assertThat(response.productId()).isEqualTo(savedProduct.getId());
            assertThat(response.name()).isEqualTo(savedProduct.getName());
            assertThat(response.price()).isEqualByComparingTo(savedProduct.getPrice());
            assertThat(response.status()).isEqualTo(savedProduct.getStatus().name());

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

            then(productRepository)
                    .should()
                    .save(productCaptor.capture());

            Product createdProduct = productCaptor.getValue();

            assertThat(createdProduct.getName()).isEqualTo(request.name());
            assertThat(createdProduct.getPrice()).isEqualByComparingTo(request.price());
            assertThat(createdProduct.getStatus()).isEqualTo(ProductStatus.ON_SALE);

            ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);

            then(inventoryRepository)
                    .should()
                    .save(inventoryCaptor.capture());

            Inventory createdInventory = inventoryCaptor.getValue();

            assertThat(createdInventory.getProduct()).isSameAs(savedProduct);
            assertThat(createdInventory.getQuantity()).isEqualTo(request.initialQuantity());
        }

        @Test
        @DisplayName("초기 수량이 0이어도 상품과 재고를 생성한다.")
        void create_product_with_zero_inventory() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest(
                            "재고 없는 상품",
                            new BigDecimal("10000"),
                            0
                    );

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            // when
            CreateProductResponse response = createProductService.create(request);

            // then
            assertThat(response.productId()).isEqualTo(savedProduct.getId());
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.price()).isEqualByComparingTo(request.price());
            assertThat(response.status()).isEqualTo(ProductStatus.ON_SALE.name());

            ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);

            then(inventoryRepository)
                    .should()
                    .save(inventoryCaptor.capture());

            Inventory createdInventory = inventoryCaptor.getValue();

            assertThat(createdInventory.getProduct()).isSameAs(savedProduct);
            assertThat(createdInventory.getQuantity()).isZero();
            assertThat(createdInventory.isOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("상품을 먼저 저장한 후 저장된 상품으로 재고를 저장한다.")
        void save_product_before_inventory() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest();

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            // when
            createProductService.create(request);

            // then
            InOrder inOrder = inOrder(productRepository, inventoryRepository);

            inOrder.verify(productRepository)
                    .save(any(Product.class));

            inOrder.verify(inventoryRepository)
                    .save(any(Inventory.class));
        }
    }

    @Nested
    @DisplayName("상품 생성 실패")
    class CreateFailure {

        @Test
        @DisplayName("상품 저장에 실패하면 재고를 저장하지 않는다.")
        void do_not_save_inventory_when_product_save_fails() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest();

            RuntimeException exception = new RuntimeException("상품 저장 실패");

            given(productRepository.save(any(Product.class))).willThrow(exception);

            // when & then
            assertThatThrownBy(() -> createProductService.create(request))
                    .isSameAs(exception);

            then(inventoryRepository)
                    .should(never())
                    .save(any(Inventory.class));
        }

        @Test
        @DisplayName("초기 수량이 음수이면 재고 생성에 실패하고 재고를 저장하지 않는다.")
        void do_not_save_inventory_when_initial_quantity_is_negative() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest(
                            "테스트 상품",
                            new BigDecimal("10000"),
                            -1
                    );

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            given(productRepository.save(any(Product.class)))
                    .willReturn(savedProduct);

            // when & then
            assertThatThrownBy(() -> createProductService.create(request))
                    .isInstanceOf(InventoryException.class);

            then(productRepository)
                    .should()
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should(never())
                    .save(any(Inventory.class));
        }

        @Test
        @DisplayName("재고 저장에 실패하면 발생한 예외를 그대로 전파한다.")
        void propagate_exception_when_inventory_save_fails() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest();

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            RuntimeException exception = new RuntimeException("재고 저장 실패");

            given(productRepository.save(any(Product.class)))
                    .willReturn(savedProduct);

            given(inventoryRepository.save(any(Inventory.class)))
                    .willThrow(exception);

            // when & then
            assertThatThrownBy(() -> createProductService.create(request))
                    .isSameAs(exception);

            then(productRepository)
                    .should()
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should()
                    .save(any(Inventory.class));
        }
    }
}

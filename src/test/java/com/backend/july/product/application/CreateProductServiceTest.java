package com.backend.july.product.application;

import static com.backend.july.fixture.ProductFixture.productWithId;
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
import com.backend.july.product.exception.ProductException;
import com.backend.july.fixture.ProductFixture;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import com.backend.july.product.presentation.dto.response.CreateProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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

        @Test
        @DisplayName("상품 가격이 최소 경계값인 1원이면 생성할 수 있다.")
        void create_product_with_minimum_price() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest(
                    "테스트 상품",
                    BigDecimal.ONE,
                    100
            );

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
            assertThat(response.price())
                    .isEqualByComparingTo(BigDecimal.ONE);

            then(productRepository)
                    .should()
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should()
                    .save(any(Inventory.class));
        }

        @Test
        @DisplayName("상품명이 최대 경계값인 100자이면 생성할 수 있다.")
        void create_product_with_maximum_name_length() {
            // given
            String name = "가".repeat(100);

            CreateProductRequest request = ProductFixture.createProductRequest(
                    name,
                    new BigDecimal("10000"),
                    100
            );

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
            assertThat(response.name()).hasSize(100);
            assertThat(response.name()).isEqualTo(name);

            then(productRepository)
                    .should()
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should()
                    .save(any(Inventory.class));
        }

        @Test
        @DisplayName("상품명이 최대 길이인 100자를 초과하면 상품과 재고를 저장하지 않는다.")
        void do_not_save_when_product_name_exceeds_maximum_length() {
            // given
            String invalidName = "가".repeat(101);

            CreateProductRequest request = ProductFixture.createProductRequest(
                    invalidName,
                    new BigDecimal("10000"),
                    100
            );

            // when & then
            assertThatThrownBy(() -> createProductService.create(request))
                    .isInstanceOf(ProductException.class);

            then(productRepository)
                    .should(never())
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should(never())
                    .save(any(Inventory.class));
        }
        @Test
        @DisplayName("소수 부분이 0인 가격은 정수 가격으로 정규화하여 생성한다.")
        void normalize_price_with_zero_fraction() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest(
                    "테스트 상품",
                    new BigDecimal("10000.00"),
                    100
            );

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            // when
            createProductService.create(request);

            // then
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

            then(productRepository)
                    .should()
                    .save(productCaptor.capture());

            Product createdProduct = productCaptor.getValue();

            assertThat(createdProduct.getPrice()).isEqualByComparingTo("10000");
            assertThat(createdProduct.getPrice().scale()).isZero();
            assertThat(createdProduct.getPrice().toPlainString()).isEqualTo("10000");
        }

        @Test
        @DisplayName("초기 수량이 Integer 최대값이어도 재고를 생성한다.")
        void create_product_with_maximum_inventory_quantity() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest(
                    "테스트 상품",
                    new BigDecimal("10000"),
                    Integer.MAX_VALUE
            );

            Product savedProduct = productWithId(
                    1L,
                    request.name(),
                    request.price()
            );

            given(productRepository.save(any(Product.class)))
                    .willReturn(savedProduct);

            // when
            createProductService.create(request);

            // then
            ArgumentCaptor<Inventory> inventoryCaptor =
                    ArgumentCaptor.forClass(Inventory.class);

            then(inventoryRepository)
                    .should()
                    .save(inventoryCaptor.capture());

            assertThat(inventoryCaptor.getValue().getQuantity())
                    .isEqualTo(Integer.MAX_VALUE);
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

        @ParameterizedTest(name = "[(index)] 상품명=\"{0}\"")
        @NullSource
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("상품명이 null 또는 공백이면 상품 저장에 실패하고 재고를 저장하지 않는다.")
        void do_not_save_inventory_when_product_name_is_invalid(String invalidName) {
            //given
            CreateProductRequest request = ProductFixture.createProductRequest(
                    invalidName,
                    new BigDecimal("10000"),
                    10
            );

            // when & then
            assertThatThrownBy(() -> createProductService.create(request))
                    .isInstanceOf(ProductException.class);

            then(productRepository)
                    .should(never())
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should(never())
                    .save(any(Inventory.class));
        }

        @Test
        @DisplayName("상품 가격이 null이면 상품과 재고를 저장하지 않는다.")
        void do_not_save_when_product_price_is_null() {
            // given
            CreateProductRequest request = ProductFixture.createProductRequest(
                    "테스트 상품",
                    null,
                    10
            );

            // when & then
            assertThatThrownBy(() -> createProductService.create(request))
                    .isInstanceOf(ProductException.class);

            then(productRepository)
                    .should(never())
                    .save(any(Product.class));

            then(inventoryRepository)
                    .should(never())
                    .save(any(Inventory.class));
        }
    }

    @ParameterizedTest(name = "[(index) 가격={0}")
    @ValueSource(doubles = {0, -10000, -1})
    @DisplayName("상품 가격이 0 또는 음수이면 상품과 재고를 저장하지 않는다.")
    void do_not_save_when_product_price_is_zero_or_negative(double invalidPrice) {
        // given
        CreateProductRequest request = ProductFixture.createProductRequest(
                "테스트 상품",
                new BigDecimal(invalidPrice),
                10
        );

        // when & then
        assertThatThrownBy(() -> createProductService.create(request))
                .isInstanceOf(ProductException.class);

        then(productRepository)
                .should(never())
                .save(any(Product.class));

        then(inventoryRepository)
                .should(never())
                .save(any(Inventory.class));
    }

    @ParameterizedTest(name = "[{index}] 가격={0}")
    @ValueSource(strings = {
            "0.1",
            "1.5",
            "9999.99",
            "10000.0001"
    })
    @DisplayName("상품 가격에 소수 값이 포함되면 상품과 재고를 저장하지 않는다.")
    void do_not_save_when_product_price_has_fraction(String invalidPrice) {
        // given
        CreateProductRequest request = ProductFixture.createProductRequest(
                "테스트 상품",
                new BigDecimal(invalidPrice),
                100
        );

        // when & then
        assertThatThrownBy(() -> createProductService.create(request))
                .isInstanceOf(ProductException.class);

        then(productRepository)
                .should(never())
                .save(any(Product.class));

        then(inventoryRepository)
                .should(never())
                .save(any(Inventory.class));
    }
}

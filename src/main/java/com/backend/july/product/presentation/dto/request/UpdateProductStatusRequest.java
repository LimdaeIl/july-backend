package com.backend.july.product.presentation.dto.request;

import com.backend.july.product.domain.ProductStatus;

public record UpdateProductStatusRequest(

        ProductStatus status
) {

}

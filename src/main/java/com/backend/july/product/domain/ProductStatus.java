package com.backend.july.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductStatus {
    ON_SALE("판매중"),
    SOLD_OUT("판매완료"),
    HIDDEN("숨김");

    private final String description;

}

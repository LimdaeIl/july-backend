package com.backend.july.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductStatus {
    ON_SALE("판매중"),
    HIDDEN("숨김"),
    DELETED("삭제");

    private final String description;

}

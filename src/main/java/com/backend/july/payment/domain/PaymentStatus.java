package com.backend.july.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentStatus {
    READY("준비"),
    APPROVED("승인"),
    FAILED("실패"),
    CANCELLED("취소");

    private final String description;
}

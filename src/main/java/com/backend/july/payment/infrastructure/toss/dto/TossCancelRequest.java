package com.backend.july.payment.infrastructure.toss.dto;

public record TossCancelRequest(
        String cancelReason
) {

    public static TossCancelRequest from(String cancelReason) {
        return new TossCancelRequest(cancelReason);
    }
}
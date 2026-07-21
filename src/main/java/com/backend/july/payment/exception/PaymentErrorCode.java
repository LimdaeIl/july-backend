package com.backend.july.payment.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    ORDER_REQUIRED(HttpStatus.BAD_REQUEST, "결제 주문은 필수입니다."),
    PAYMENT_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "결제 키는 필수입니다."),
    PAYMENT_AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, "결제 금액은 필수입니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액은 0보다 커야 합니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "주문 금액과 결제 금액이 일치하지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.CONFLICT, "현재 결제 상태에서는 요청한 작업을 수행할 수 없습니다."),
    FAILURE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "결제 실패 사유는 필수입니다."),
    CANCELLATION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "결제 취소 사유는 필수입니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 주문의 결제 정보가 이미 존재합니다."),
    APPROVED_AT_REQUIRED(HttpStatus.BAD_REQUEST, "결제 승인 시각은 필수입니다."),
    FAILED_AT_REQUIRED(HttpStatus.BAD_REQUEST, "결제 실패 시각은 필수입니다."),
    CANCELLED_AT_REQUIRED(HttpStatus.BAD_REQUEST, "결제 취소 시각은 필수입니다."),
    INVALID_CANCELLATION_REASON(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 취소 사유입니다."),
    INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 키입니다."),
    INVALID_FAILURE_REASON(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 실패 사유입니다.");


    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}

package com.backend.july.payment.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    ORDER_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 주문은 필수입니다."
    ),

    PAYMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "결제 정보를 찾을 수 없습니다."
    ),

    PAYMENT_PREPARATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "결제 준비 정보를 찾을 수 없습니다."
    ),

    PAYMENT_KEY_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 키는 필수입니다."
    ),

    PAYMENT_AMOUNT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 금액은 필수입니다."
    ),

    INVALID_PAYMENT_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "결제 금액은 0보다 커야 합니다."
    ),

    PAYMENT_AMOUNT_MISMATCH(
            HttpStatus.CONFLICT,
            "주문 금액과 결제 금액이 일치하지 않습니다."
    ),

    TOSS_PAYMENT_AMOUNT_MISMATCH(
            HttpStatus.CONFLICT,
            "결제 승인 금액과 주문 금액이 일치하지 않습니다."
    ),

    TOSS_ORDER_ID_MISMATCH(
            HttpStatus.CONFLICT,
            "결제 승인 주문번호가 일치하지 않습니다."
    ),

    TOSS_PAYMENT_KEY_MISMATCH(
            HttpStatus.CONFLICT,
            "결제 승인 키가 일치하지 않습니다."
    ),

    TOSS_PAYMENT_NOT_DONE(
            HttpStatus.CONFLICT,
            "결제 승인이 완료되지 않았습니다."
    ),

    INVALID_PAYMENT_STATUS(
            HttpStatus.CONFLICT,
            "현재 결제 상태에서는 요청한 작업을 수행할 수 없습니다."
    ),

    PAYMENT_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "해당 주문의 결제 정보가 이미 존재합니다."
    ),

    PAYMENT_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "본인의 결제만 처리할 수 있습니다."
    ),

    PAYMENT_ORDER_MISMATCH(
            HttpStatus.CONFLICT,
            "결제 정보와 주문 정보가 일치하지 않습니다."
    ),

    FAILURE_REASON_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 실패 사유는 필수입니다."
    ),

    CANCELLATION_REASON_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 취소 사유는 필수입니다."
    ),

    APPROVED_AT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 승인 시각은 필수입니다."
    ),

    FAILED_AT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 실패 시각은 필수입니다."
    ),

    CANCELLED_AT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "결제 취소 시각은 필수입니다."
    ),

    INVALID_CANCELLATION_REASON(
            HttpStatus.BAD_REQUEST,
            "유효하지 않은 결제 취소 사유입니다."
    ),

    INVALID_PAYMENT_KEY(
            HttpStatus.BAD_REQUEST,
            "유효하지 않은 결제 키입니다."
    ),

    INVALID_FAILURE_REASON(
            HttpStatus.BAD_REQUEST,
            "유효하지 않은 결제 실패 사유입니다."
    ),
    TOSS_PAYMENT_CONFIRM_FAILED(
            HttpStatus.BAD_GATEWAY,
            "토스 결제 승인에 실패했습니다."
    ),

    TOSS_PAYMENT_CONFIRM_UNCERTAIN(
            HttpStatus.SERVICE_UNAVAILABLE,
            "결제 승인 결과를 확인할 수 없습니다. 잠시 후 다시 확인해 주세요."
    ),

    TOSS_PAYMENT_INVALID_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "토스 결제 승인 응답이 올바르지 않습니다."
    );


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


package com.backend.july.payment.exception;

import com.backend.july.common.exception.CommonException;
import com.backend.july.common.exception.ErrorCode;

public class PaymentException extends CommonException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(
            ErrorCode errorCode,
            Object... arguments
    ) {
        super(errorCode, arguments);
    }
}

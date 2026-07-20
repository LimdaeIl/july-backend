package com.backend.july.order.exception;

import com.backend.july.common.exception.CommonException;
import com.backend.july.common.exception.ErrorCode;

public class OrderException extends CommonException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(ErrorCode errorCode, Object... arguments) {
        super(errorCode, arguments);
    }
}

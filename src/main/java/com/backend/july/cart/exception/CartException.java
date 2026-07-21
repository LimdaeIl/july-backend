package com.backend.july.cart.exception;

import com.backend.july.common.exception.CommonException;
import com.backend.july.common.exception.ErrorCode;

public class CartException extends CommonException {

    public CartException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CartException(ErrorCode errorCode, Object... arguments) {
        super(errorCode, arguments);
    }
}

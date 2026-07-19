package com.backend.july.product.exception;

import com.backend.july.common.exception.CommonException;
import com.backend.july.common.exception.ErrorCode;

public class ProductException  extends CommonException {

    public ProductException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProductException(ErrorCode errorCode, Object... arguments) {
        super(errorCode, arguments);
    }
}


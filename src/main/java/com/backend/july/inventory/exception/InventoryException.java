package com.backend.july.inventory.exception;

import com.backend.july.common.exception.CommonException;
import com.backend.july.common.exception.ErrorCode;

public class InventoryException extends CommonException {

    public InventoryException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InventoryException(ErrorCode errorCode, Object... arguments) {
        super(errorCode, arguments);
    }
}

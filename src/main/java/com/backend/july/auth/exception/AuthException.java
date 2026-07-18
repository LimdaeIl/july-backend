package com.backend.july.auth.exception;

import com.backend.july.common.exception.CommonException;

public class AuthException extends CommonException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(AuthErrorCode errorCode, Object... arguments) {
        super(errorCode, arguments);
    }

}

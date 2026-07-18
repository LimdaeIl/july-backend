package com.backend.july.member.exception;

import com.backend.july.common.exception.CommonException;
import com.backend.july.common.exception.ErrorCode;

public class MemberException extends CommonException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(ErrorCode errorCode, Object... arguments) {
        super(errorCode, arguments);
    }
}

package com.backend.july.common.exception;

import com.backend.july.common.response.ErrorResponse;
import com.backend.july.common.response.ErrorResponse.InvalidParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_INVALID_VALUE_MESSAGE = "요청 값이 올바르지 않습니다.";

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException exception,
            HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();

        log.warn(
                "비즈니스 예외 발생. type={}, status={}, method={}, path={}, message={}",
                errorCode.name(),
                errorCode.status().value(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return problemResponse(errorCode,
                ErrorResponse.of(errorCode, exception.getMessage(), request)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<InvalidParameter> invalidParameters = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> InvalidParameter.of(
                        error.getField(),
                        error.getDefaultMessage() == null
                                ? DEFAULT_INVALID_VALUE_MESSAGE
                                : error.getDefaultMessage()
                ))
                .distinct()
                .sorted(Comparator.comparing(InvalidParameter::name))
                .toList();

        return validationErrorResponse(CommonErrorCode.INVALID_INPUT_VALUE, request,
                invalidParameters);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolationException(ConstraintViolationException exception,
            HttpServletRequest request) {
        List<InvalidParameter> invalidParameters = exception
                .getConstraintViolations()
                .stream()
                .map(violation -> InvalidParameter.of(
                        extractParameterName(
                                violation
                                        .getPropertyPath()
                                        .toString()
                        ),
                        violation.getMessage()
                ))
                .distinct()
                .sorted(Comparator.comparing(InvalidParameter::name))
                .toList();

        return validationErrorResponse(CommonErrorCode.INVALID_INPUT_VALUE, request,
                invalidParameters);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse>
    handleMissingRequestParameterException(MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        List<InvalidParameter> invalidParameters =
                List.of(InvalidParameter.of(exception.getParameterName(), "필수 요청 파라미터입니다."));

        return validationErrorResponse(CommonErrorCode.MISSING_REQUEST_PARAMETER, request,
                invalidParameters);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        List<InvalidParameter> invalidParameters = List.of(
                InvalidParameter.of(exception.getName(), "요청 값의 타입이 올바르지 않습니다."));

        return validationErrorResponse(CommonErrorCode.TYPE_MISMATCH, request, invalidParameters);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
    handleHttpMessageNotReadableException(HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        log.debug(
                "요청 본문 파싱 실패. method={}, path={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorCode errorCode = CommonErrorCode.INVALID_JSON_FORMAT;

        return problemResponse(errorCode, ErrorResponse.of(errorCode, request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;

        return problemResponse(errorCode, ErrorResponse.of(errorCode, request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>
    handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        log.warn(
                "접근 권한 거부. method={}, path={}",
                request.getMethod(),
                request.getRequestURI()
        );

        ErrorCode errorCode = CommonErrorCode.ACCESS_DENIED;

        return problemResponse(errorCode, ErrorResponse.of(errorCode, request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolationException(DataIntegrityViolationException exception,
            HttpServletRequest request) {
        log.warn(
                "데이터 무결성 제약 조건 위반. method={}, path={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                getMostSpecificCauseMessage(exception)
        );

        ErrorCode errorCode = CommonErrorCode.DATA_INTEGRITY_VIOLATION;

        return problemResponse(errorCode, ErrorResponse.of(errorCode, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error(
                "처리되지 않은 예외 발생. method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

        return problemResponse(errorCode, ErrorResponse.of(errorCode, request));
    }

    // 검증 실패 목록을 포함하는 에러 응답을 생성
    private ResponseEntity<ErrorResponse> validationErrorResponse(ErrorCode errorCode,
            HttpServletRequest request, List<InvalidParameter> invalidParameters) {
        ErrorResponse response = ErrorResponse.withErrors(errorCode, request, invalidParameters);

        return problemResponse(errorCode, response);
    }

    // 에러 응답의 상태 코드와 미디어 타입을 통일
    private ResponseEntity<ErrorResponse> problemResponse(ErrorCode errorCode,
            ErrorResponse response) {
        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }

    // 검증 경로에서 마지막 파라미터 이름만 추출
    private String extractParameterName(String propertyPath) {
        int lastDotIndex = propertyPath.lastIndexOf('.');

        if (lastDotIndex < 0) {
            return propertyPath;
        }

        return propertyPath.substring(lastDotIndex + 1);
    }

    // 데이터 무결성 예외의 가장 구체적인 메시지를 추출합니다.
    private String getMostSpecificCauseMessage(DataIntegrityViolationException exception) {
        Throwable cause = exception.getMostSpecificCause();

        if (cause == null || cause.getMessage() == null) {
            return exception.getMessage();
        }

        return cause.getMessage();
    }
}


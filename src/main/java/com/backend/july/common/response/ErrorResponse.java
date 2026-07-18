package com.backend.july.common.response;

import com.backend.july.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RFC 9457 (Problem Details for HTTP APIs) 표준 규격을 준수하는 에러 응답 객체
 */
public record ErrorResponse(
        String type,           // RFC 9457: 에러 타입을 식별하는 URI 참조 (기본값: about:blank)
        String title,          // RFC 9457: 발생한 에러의 비즈니스적 요약 (예: INVALID_INPUT_VALUE)
        int status,            // RFC 9457: 원본 HTTP 상태 코드
        String detail,         // RFC 9457: 에러의 구체적인 발생 원인에 대한 상세 설명
        String instance,       // RFC 9457: 에러가 발생한 구체적인 엔드포인트 URI 경로
        LocalDateTime timestamp, // 시스템 확장 필드: 에러 발생 일시
        List<InvalidParameter> invalidParameters // RFC 9457 확장 필드: 유효성 검증 실패 파라미터 목록
) {

    // 상세 내용(detail)을 직접 명시하는 팩토리 메서드
    public static ErrorResponse of(ErrorCode errorCode, String detail, HttpServletRequest request) {
        return new ErrorResponse(
                "about:blank",
                errorCode.name(),
                errorCode.status().value(),
                detail,
                request.getRequestURI(),
                LocalDateTime.now(),
                List.of()
        );
    }

    // ErrorCode의 기본 메시지를 사용하는 팩토리 메서드
    public static ErrorResponse of(ErrorCode errorCode, HttpServletRequest request) {
        return of(errorCode, errorCode.message(), request);
    }

    // 유효성 검증 예외 목록을 포함하는 팩토리 메서드
    public static ErrorResponse withErrors(ErrorCode errorCode, HttpServletRequest request, List<InvalidParameter> invalidParameters) {
        return new ErrorResponse(
                "about:blank",
                errorCode.name(),
                errorCode.status().value(),
                errorCode.message(),
                request.getRequestURI(),
                LocalDateTime.now(),
                invalidParameters
        );
    }

    // RFC 9457 확장 규격을 따르는 개별 오류 정보 구조체
    public record InvalidParameter(
            String name,   // 에러가 발생한 필드명
            String reason  // 에러 발생 사유
    ) {
        public static InvalidParameter of(String name, String reason) {
            return new InvalidParameter(name, reason);
        }
    }
}

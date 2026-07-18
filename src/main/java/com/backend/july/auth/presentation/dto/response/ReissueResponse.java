package com.backend.july.auth.presentation.dto.response;

public record ReissueResponse(
        Long id,
        String newAccessToken
) {

    public static ReissueResponse of(Long id, String newAccessToken) {
        return new  ReissueResponse(id, newAccessToken);
    }
}

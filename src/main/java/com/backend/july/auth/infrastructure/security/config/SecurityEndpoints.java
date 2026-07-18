package com.backend.july.auth.infrastructure.security.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityEndpoints {

    public static final String[] AUTH_PUBLIC = {
            "/api/v1/auth/**"
    };

    public static final String[] API_DOCS_PUBLIC = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/docs/**"
    };

    public static final String[] SYSTEM_PUBLIC = {
            "/error"
    };
}

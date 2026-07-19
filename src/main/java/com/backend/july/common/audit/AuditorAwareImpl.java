package com.backend.july.common.audit;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.exception.CommonErrorCode;
import com.backend.july.common.exception.CommonException;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<Long> {

    private static final long SYSTEM_AUDITOR_ID = 0L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of(SYSTEM_AUDITOR_ID);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginMember loginMember) {
            return Optional.of(loginMember.memberId());
        }

        String principalType = principal == null ? "null" : principal.getClass().getName();

        throw new CommonException(CommonErrorCode.UNAUTHENTICATED_MEMBER, principalType);
    }
}


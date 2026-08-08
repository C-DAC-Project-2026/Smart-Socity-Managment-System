package com.society.security;

import com.society.exception.ForbiddenException;
import com.society.exception.UnauthorizedException;
import com.society.util.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for "who is making this request and which society
 * do they belong to". Every service method that touches tenant-owned data
 * MUST get its societyId from here — never from a request body, path
 * variable, or query parameter. This is what makes cross-tenant access
 * impossible even if a client tampers with the request.
 *
 * Backed by CustomUserDetails, which CustomUserDetailsService reloads from
 * the database on every single request (JwtAuthenticationFilter re-resolves
 * the user by email each time). That means a society being suspended, or a
 * user being deactivated, takes effect on the very next request — we never
 * rely on stale claims baked into an already-issued JWT for authorization.
 */
@Component
public final class SecurityUtils {

    private CustomUserDetails currentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new UnauthorizedException("No authenticated user in the current request");
        }
        return cud;
    }

    public Long getCurrentUserId() {
        return currentUserDetails().getUserId();
    }

    public String getCurrentRole() {
        return currentUserDetails().getRole();
    }

    public boolean isSuperAdmin() {
        return AppConstants.ROLE_SUPER_ADMIN.equals(getCurrentRole());
    }

    /**
     * The society id every tenant-scoped query must filter by.
     * Throws for SUPER_ADMIN, who has no society — callers on SUPER_ADMIN-only
     * endpoints should never call this.
     */
    public Long getCurrentSocietyId() {
        CustomUserDetails cud = currentUserDetails();
        if (cud.getSocietyId() == null) {
            // The caller IS authenticated (we got this far) — they just aren't
            // scoped to any society. That's a 403, not a 401: it must never be
            // treated by the frontend as "your session/token is invalid".
            throw new ForbiddenException("This account is not associated with a society");
        }
        return cud.getSocietyId();
    }
}

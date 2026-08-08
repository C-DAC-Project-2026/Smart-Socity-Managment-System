package com.society.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the caller IS authenticated (a valid JWT was presented and
 * verified) but is not entitled to the specific resource/action requested
 * — e.g. their account has no society attached, or they're trying to touch
 * a record that belongs to someone else.
 *
 * This is intentionally separate from UnauthorizedException (401), which
 * means "no valid authentication at all". The distinction matters a lot on
 * the frontend: api/axios.js force-clears the session and redirects to
 * /login on ANY 401, on the assumption that 401 always means "your token is
 * missing/invalid/expired". If a business-rule check reuses 401 for "you're
 * logged in but can't do this", a perfectly valid session gets nuked for a
 * reason that has nothing to do with the token. That was the root cause of
 * "Society Admin logs in, then is logged out within a second": the very
 * first authenticated call the dashboard makes could throw a 401 for a
 * reason unrelated to the token itself, and the frontend interpreted that
 * as an expired session.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}

package com.society.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CustomUserDetails custom = (CustomUserDetails) userDetails;
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("userId", custom.getUserId())
            .claim("role",   custom.getRole())
            .claim("societyId", custom.getSocietyId())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
            .parseClaimsJws(token).getBody().getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
            .parseClaimsJws(token).getBody();
        return claims.get("userId", Long.class);
    }

    /**
     * NOTE: this reads the societyId claim embedded in the token at login time.
     * It is exposed for logging/debugging/stateless clients only. Backend
     * authorization decisions must NEVER use this — they must use
     * SecurityUtils.getCurrentSocietyId(), which is re-derived from the
     * database on every request (so a suspended society or a society
     * transfer takes effect immediately instead of waiting for token expiry).
     */
    public Long getSocietyIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
            .parseClaimsJws(token).getBody();
        return claims.get("societyId", Long.class);
    }

    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return getUserIdFromToken(token);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

package com.geosecure.attendance.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.geosecure.attendance.security.UserPrincipal;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Issues and validates access/refresh JWTs.
 *
 * Access-token claims:
 *   sub  = user id (as String)
 *   role = role name, e.g. "faculty"
 *
 * Access and refresh tokens are signed with independent secrets so a
 * leaked access token cannot be used to forge a refresh token.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String accessTokenSecret;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-secret}")
    private String refreshTokenSecret;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey accessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
    }

    private SecretKey refreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshTokenSecret));
    }

    // --- Token generation ---

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildAccessToken(principal.getId(), principal.getRole());
    }

    /** Used when re-issuing a token after a refresh, where role is re-read from the DB. */
    public String generateAccessToken(Integer userId, String role) {
        return buildAccessToken(userId, role);
    }

    private String buildAccessToken(Integer userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(accessKey())
                .compact();
    }

    /** Long-lived refresh token. Only embeds the user id; role is re-loaded from the DB on refresh. */
    public String generateRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(refreshKey())
                .compact();
    }

    // --- Token validation ---

    public boolean validateAccessToken(String token) {
        return parseToken(token, accessKey()) != null;
    }

    public boolean validateRefreshToken(String token) {
        return parseToken(token, refreshKey()) != null;
    }

    // --- Claims extraction ---

    public Integer extractUserIdFromAccessToken(String token) {
        Claims c = parseToken(token, accessKey());
        return c == null ? null : Integer.valueOf(c.getSubject());
    }

    public Integer extractUserIdFromRefreshToken(String token) {
        Claims c = parseToken(token, refreshKey());
        return c == null ? null : Integer.valueOf(c.getSubject());
    }

    public String extractRoleFromAccessToken(String token) {
        Claims c = parseToken(token, accessKey());
        return c == null ? null : c.get("role", String.class);
    }

    private Claims parseToken(String token, SecretKey key) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (MalformedJwtException e) {
            log.warn("JWT: malformed token - {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT: token expired - {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT: unsupported token - {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT: empty or null token - {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("JWT: invalid signature - {}", e.getMessage());
        }
        return null;
    }
}

package com.fernirx.sneakerapi.security.jwt;

import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    public static final String JWT_ACCESS_TOKEN = "access_token";
    public static final String JWT_REFRESH_TOKEN = "refresh_token";
    public static final String JWT_RESET_PASSWORD_TOKEN = "reset_password_token";
    public static final String JWT_CLAIMS_TYPE = "type";
    public static final String JWT_CLAIMS_EMAIL = "email";
    public static final String JWT_CLAIMS_AUTHORITIES = "authorities";

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64URL.decode(jwtProperties.getSecret())
        );
    }

    public String generateAccessToken(UserTokenPayload payload) {
        return buildToken(JWT_ACCESS_TOKEN, payload);
    }

    public String generateRefreshToken(UserTokenPayload payload) {
        return buildToken(JWT_REFRESH_TOKEN, payload);
    }

    public String generateResetPasswordToken(UserTokenPayload payload) {
        return buildToken(JWT_RESET_PASSWORD_TOKEN, payload);
    }

    public void validateAccessToken(String token) {
        validateToken(token, JWT_ACCESS_TOKEN);
    }

    public void validateRefreshToken(String token) {
        validateToken(token, JWT_REFRESH_TOKEN);
    }

    public void validateResetPasswordToken(String token) {
        validateToken(token, JWT_RESET_PASSWORD_TOKEN);
    }

    public String extractSubject(String token) {
        return parseToken(token).getSubject();
    }

    public String extractEmail(String token) {
        Object email = parseToken(token).get(JWT_CLAIMS_EMAIL);
        return email != null ? email.toString() : null;
    }

    public LocalDateTime extractExpiration(String token) {
        return parseToken(token).getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String extractJti(String token) {
        return parseToken(token).getId();
    }

    private String buildToken(String type, UserTokenPayload payload) {
        Date nowDate = new Date();
        Date expirationDate;
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWT_CLAIMS_TYPE, type);
        claims.put(JWT_CLAIMS_EMAIL, payload.email());
        switch (type) {
            case JWT_ACCESS_TOKEN:
                expirationDate =
                        new Date(nowDate.getTime() + jwtProperties.getAccess().getExpiration().toMillis());
                claims.put(JWT_CLAIMS_AUTHORITIES, payload.authorities());
                break;

            case JWT_REFRESH_TOKEN:
                expirationDate =
                        new Date(nowDate.getTime() + jwtProperties.getRefresh().getExpiration().toMillis());
                break;

            case JWT_RESET_PASSWORD_TOKEN:
                expirationDate =
                        new Date(nowDate.getTime() + jwtProperties.getResetPassword().getExpiration().toMillis());
                break;

            default:
                throw new IllegalStateException("Invalid token type: " + type);
        }
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(payload.id().toString())
                .claims(claims)
                .issuedAt(nowDate)
                .expiration(expirationDate)
                .issuer(jwtProperties.getIssuer())
                .signWith(secretKey)
                .compact();
    }

    private void validateToken(String token, String type) {
        Claims claims = parseToken(token);
        Object typeObj = claims.get(JWT_CLAIMS_TYPE);
        if (typeObj == null || !type.equals(typeObj.toString())) {
            throw SecurityCustomException.invalid("label.token");
        }
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw SecurityCustomException.invalid("label.token");
        }
    }
}

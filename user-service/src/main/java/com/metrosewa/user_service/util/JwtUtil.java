package com.metrosewa.user_service.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY =
            "mySecretKeymySecretKeymySecretKey123456";

    private final long EXPIRATION = 86400000;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String mobileNumber) {

        return Jwts.builder()
                .setSubject(mobileNumber)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractMobile(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String mobile) {

        return extractMobile(token).equals(mobile);
    }
}

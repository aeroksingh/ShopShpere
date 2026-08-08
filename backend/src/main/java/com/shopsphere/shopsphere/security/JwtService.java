package com.shopsphere.shopsphere.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Generate an access token for the authenticated user.
     */
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(
                userDetails,
                jwtProperties.getExpirationMs()
        );
    }

    /**
     * Generate a refresh token for the authenticated user.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(
                userDetails,
                jwtProperties.getRefreshExpirationMs()
        );
    }

    /**
     * Build and sign a JWT.
     */
    private String buildToken(
            UserDetails userDetails,
            long expirationMs
    ) {

        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the email/username from the JWT subject.
     */
    public String extractEmail(String token) {
        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    /**
     * Validate the JWT against the current user.
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        try {

            String email = extractEmail(token);

            return email.equals(userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (ExpiredJwtException e) {

            log.debug(
                    "JWT expired: {}",
                    e.getMessage()
            );

            return false;

        } catch (Exception e) {

            log.warn(
                    "JWT validation failed: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    /**
     * Check whether the JWT has expired.
     */
    private boolean isTokenExpired(String token) {

        Date expiration = extractClaim(
                token,
                Claims::getExpiration
        );

        return expiration.before(new Date());
    }

    /**
     * Extract any claim from the JWT.
     */
    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    /**
     * Create the signing key from the Base64 encoded secret.
     */
    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(
                        jwtProperties.getSecret()
                );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
package com.linkedin.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
@Slf4j
/**
 * JWT authentification Filter
 *
 *
 * Applied to all routes except /api/v1/auth/**
 *
 * 1. Extract JWT from Authorization header
 * 2. Validate JWT signature and expire
 * 3. Extract userId from token claims
 * 4. Add userId to request headers for downstream services
 * 5. Forward request to the correct service
 *
 * If JWT is incorrect or missing - 401 Unauthorized
 */
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret-key}")
    private String secretKey;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) ->  {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // No token reject
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Authorization header not found");
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = extractClaims(token);
                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();
                log.info("JWT validated for user {} with email {}", userId, email);

                // Add userId to request header from downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request( r -> r.header("X-User-Id", userId).header("X-User-Email", email).build())
                        .build();
                return chain.filter(modifiedExchange);

            }catch (Exception ex){
                log.warn("Authorization header not found {}", ex.getMessage());
                return unauthorized(exchange);
            }
        });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public  static class Config {

    }

}

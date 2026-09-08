package com.project.TPApiGateway.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RouteValidator routeValidator;

    public AuthenticationFilter() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            // 1. Check if the route even requires auth (ignore public routes)
            if (routeValidator.isSecured.test(request)) {

                // 2. Extract Authorization Header
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                String username;

                // 3. Validate Token
                try {
                    username = jwtUtil.extractUsername(token);
                    if (!jwtUtil.validateToken(token)) {
                        return onError(exchange, "Token is expired", HttpStatus.UNAUTHORIZED);
                    }
                } catch (RuntimeException exception) {
                    return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                }
                request = exchange.getRequest().mutate().header("X-User-Id", username).build();
            }
            // 4. Continue the chain if valid
            return chain.filter(exchange.mutate().request(request).build());
        });
    }
    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange, String err, HttpStatus status){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
    public static class Config {
        // Configuration properties can go here
    }
}

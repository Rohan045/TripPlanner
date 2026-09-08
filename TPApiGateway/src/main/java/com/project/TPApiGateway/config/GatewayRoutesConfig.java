package com.project.TPApiGateway.config;

import com.project.TPApiGateway.utility.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Routes defined in code so they are registered when the gateway starts.
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator userServiceRoute(RouteLocatorBuilder builder,
                                 AuthenticationFilter authenticationFilter) {
        return builder.routes()
                .route("user-service", route -> route
                        .path("/user/**")
                        .filters(filters -> filters.filter(
                                authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USERSERVICE"))
                .build();
    }
}

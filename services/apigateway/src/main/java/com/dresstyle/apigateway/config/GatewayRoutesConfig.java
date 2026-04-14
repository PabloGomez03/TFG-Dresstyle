package com.dresstyle.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-api", 
                r -> r
                .path("/api/auth/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://auth-service"))
                .route("catalog-service-api", 
                r -> r
                .path("/api/catalog/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://catalog-service"))
                .route("orderservice-api",
                r -> r
                .path("/api/orders/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://orderservice"))
                .build();
                
                
    }
}

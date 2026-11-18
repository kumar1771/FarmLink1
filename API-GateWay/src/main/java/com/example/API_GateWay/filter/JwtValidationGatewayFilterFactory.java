package com.example.API_GateWay.filter;

import com.example.API_GateWay.Method.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.JwtValidationConfig> {

    @Value("${app.api-key}")
    private String API_KEY;

    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webBuilder) {
        super(JwtValidationConfig.class);
        this.webClient = webBuilder.baseUrl("lb://auth-service").build();
    }

    @Override
    public GatewayFilter apply(JwtValidationConfig config) {
        return (exchange, chain) -> {

            // ✅ Handle CORS preflight FIRST (very important)
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // ❌ No token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            return webClient.get()
                    .uri("/Users/validate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-Api-Key", API_KEY)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .flatMap(response -> {

                        // ❌ Invalid token
                        if (!response.isValid()) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        // ✅ Role validation
                        List<String> allowedRoles = Arrays.stream(config.getAllowedRoles().split(","))
                                .map(String::trim)
                                .toList();

                        boolean roleAllowed = allowedRoles.stream()
                                .anyMatch(r -> r.equalsIgnoreCase(response.getRole()));

                        if (!roleAllowed) {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }

                        // ✅ Add user details to headers
                        var modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(response.getUsername()))
                                .header("X-User-Role", response.getRole())
                                .build();

                        var modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();

                        // ✅ Continue request
                        return chain.filter(modifiedExchange);
                    })
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    // Configuration class for allowed roles
    public static class JwtValidationConfig {
        private String allowedRoles;

        public String getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(String allowedRoles) {
            this.allowedRoles = allowedRoles;
        }
    }
}

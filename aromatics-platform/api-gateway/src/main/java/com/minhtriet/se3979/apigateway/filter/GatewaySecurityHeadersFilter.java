package com.minhtriet.se3979.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewaySecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Xóa mọi header nội bộ nếu client bên ngoài cố tình gửi vào qua API Gateway
        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    if (httpHeaders.containsKey("X-Internal-Secret")) {
                        log.warn("Phát hiện và chặn header giả mạo X-Internal-Secret từ IP: {}",
                                exchange.getRequest().getRemoteAddress());
                        httpHeaders.remove("X-Internal-Secret");
                    }
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Email");
                    httpHeaders.remove("X-User-Role");
                })
                .build();

        return chain.filter(exchange.mutate().request(sanitizedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

package my.com.liberty.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import my.com.liberty.apigateway.service.DataInitService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class CustomRouteFilter implements GatewayFilter {

    private final DataInitService dataInitService;
    private final Long apiRouteId;

    public CustomRouteFilter(DataInitService dataInitService, Long apiRouteId) {
        this.dataInitService = dataInitService;
        this.apiRouteId = apiRouteId;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO: restricted routes
        return chain.filter(exchange);
    }
}

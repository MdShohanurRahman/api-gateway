package my.com.liberty.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.observability.DefaultSignalListener;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .tap(() -> new DefaultSignalListener<>() {
                    @Override
                    public void doOnSubscription() throws Throwable {
                        log.info("[{}] start in {}", exchange.getRequest().getPath(), LocalDateTime.now());
                    }

                    @Override
                    public void doOnComplete() throws Throwable {
                        log.info("[{}] executed in {} ", exchange.getRequest().getPath(), LocalDateTime.now());
                    }

                    @Override
                    public void doOnError(Throwable error) throws Throwable {
                        log.warn("[{}] errored in {}", exchange.getRequest().getPath(), LocalDateTime.now());
                    }
                });
    }

}
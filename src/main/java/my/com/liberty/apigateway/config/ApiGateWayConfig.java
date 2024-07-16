package my.com.liberty.apigateway.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.AllArgsConstructor;
import my.com.liberty.apigateway.entity.ApiRoute;
import my.com.liberty.apigateway.filter.CustomRouteFilter;
import my.com.liberty.apigateway.repository.ApiRouteRepository;
import my.com.liberty.apigateway.service.DataInitService;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;
import org.zalando.logbook.netty.LogbookServerHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.util.List;

@Configuration
@AllArgsConstructor
public class ApiGateWayConfig {

    private final ApiRouteRepository routeRepository;
    private final DataInitService dataInitService;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        dataInitService.init();
        RouteLocatorBuilder.Builder routes = builder.routes();
        List<ApiRoute> apiRouteList = routeRepository.findAll();

        for (ApiRoute route : apiRouteList) {
            if (route.getAuthKey() != null && !route.getAuthKey().isEmpty()) {
                routes.route(route.getId().toString(), r -> r
                        .path(route.getPath())
                        .and()
                        .header("AUTH-KEY", route.getAuthKey())
                        .filters(f -> f
                                .rewritePath(route.getPath().replace("**", "(?<remaining>.*)"), "/${remaining}")
                                .filter(new CustomRouteFilter(dataInitService, route.getId()))
                        )
                        .uri(route.getUri()));
            } else {
                routes.route(route.getId().toString(), r -> r
                        .path(route.getPath())
                        .filters(f -> f
                                .rewritePath(route.getPath().replace("**", "(?<remaining>.*)"), "/${remaining}")
                        )
                        .uri(route.getUri()));
            }
        }

        return routes.build();
    }

    @Bean
    public Logbook logbook() {
        return Logbook.builder().build();
    }

    @Bean
    HttpClient httpClient(Logbook logbook) {
        return HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .doOnConnected(
                        (connection -> connection.addHandlerLast(new LogbookClientHandler(logbook)))
                );
    }

    @Bean
    public HttpServer httpServer(Logbook logbook) {
        return HttpServer.create()
                .doOnConnection(connection -> connection.addHandlerLast(new LogbookServerHandler(logbook)));
    }

}

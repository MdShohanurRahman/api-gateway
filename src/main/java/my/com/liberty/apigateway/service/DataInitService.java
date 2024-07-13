package my.com.liberty.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.com.liberty.apigateway.entity.ApiRoute;
import my.com.liberty.apigateway.entity.RouteRestriction;
import my.com.liberty.apigateway.repository.ApiRouteRepository;
import my.com.liberty.apigateway.repository.RouteRestrictionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitService {

    private final ApiRouteRepository apiRouteRepository;
    private final RouteRestrictionRepository restrictionRepository;
    private final ObjectMapper objectMapper;
    private final Map<Long, List<RouteRestriction>> restrictionCache = new ConcurrentHashMap<>();

    @Value("${spring.application.api-route-json-path}")
    private String apiRouteJsonPath;

    public void init() {
        this.loadApiRoutes();
        this.loadRestrictions();
    }

    private void loadApiRoutes() {
        Resource resource = new ClassPathResource(apiRouteJsonPath);
        List<ApiRoute> apiRoutes = new ArrayList<>();

        try {
            log.info("read apiRoute json file");
            apiRoutes = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (Exception ex) {
            log.error("failed to load json");
            log.error(ex.getMessage());
        }

        try {
            log.info("saving api-routes into db");
            apiRouteRepository.saveAll(apiRoutes);
        } catch (Exception ex) {
            log.error("failed to save api-routes into db");
            log.error(ex.getMessage());
        }
    }

    private void loadRestrictions() {
        List<RouteRestriction> restrictions = restrictionRepository.findAll();
        restrictionCache.clear();
        for (RouteRestriction restriction : restrictions) {
            restrictionCache.put(restriction.getRoute().getId(), restrictions);
        }
    }

    public List<RouteRestriction> getRestrictedMethods(Long routeId) {
        return restrictionCache.getOrDefault(routeId, List.of());
    }

    public void refreshRestrictions() {
        loadRestrictions();
    }
}


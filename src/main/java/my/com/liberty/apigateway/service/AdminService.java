package my.com.liberty.apigateway.service;

import lombok.RequiredArgsConstructor;
import my.com.liberty.apigateway.entity.ApiRoute;
import my.com.liberty.apigateway.repository.ApiRouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ApiRouteRepository apiRouteRepository;
    private final DataInitService dataInitService;

    public List<ApiRoute> apiRouteList() {
        return apiRouteRepository.findAll();
    }

    public void refreshRestrictions() {
        dataInitService.refreshRestrictions();
    }
}

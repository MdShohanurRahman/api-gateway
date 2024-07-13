package my.com.liberty.apigateway.repository;

import my.com.liberty.apigateway.entity.ApiRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiRouteRepository extends JpaRepository<ApiRoute, Long> {
}

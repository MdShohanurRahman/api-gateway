package my.com.liberty.apigateway.repository;

import my.com.liberty.apigateway.entity.RouteRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRestrictionRepository extends JpaRepository<RouteRestriction, Long> {
    List<RouteRestriction> findByRouteId(Long routeId);
}

package my.com.liberty.apigateway.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.AllArgsConstructor;
import my.com.liberty.apigateway.entity.ApiRoute;
import my.com.liberty.apigateway.filter.CustomRouteFilter;
import my.com.liberty.apigateway.repository.ApiRouteRepository;
import my.com.liberty.apigateway.service.DataInitService;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;
import org.zalando.logbook.netty.LogbookServerHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

@Configuration
@AllArgsConstructor
public class AppConfig {

    private final ApiRouteRepository routeRepository;
    private final DataInitService dataInitService;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

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

    @Bean KeyPair selfSingKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new RuntimeException("selfSingKeyPair error : " + ex.getMessage());
        }
    }

    @Bean
    public X509Certificate selfSignCertificate(KeyPair selfSingKeyPair) {
        try {
            X500Name issuerName = new X500Name("CN=Company Sdn Bhd, C=MY, O=Company Sdn Bhd");
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24);
            Date notAfter = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365);

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    issuerName,
                    serial,
                    notBefore,
                    notAfter,
                    issuerName,
                    selfSingKeyPair.getPublic()
            );

            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA")
                    .build(selfSingKeyPair.getPrivate());
            X509CertificateHolder certHolder = certBuilder.build(contentSigner);

            return new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certHolder);
        } catch (Exception ex) {
            throw new RuntimeException("selfSignCertificate error : " + ex.getMessage());
        }
    }
}

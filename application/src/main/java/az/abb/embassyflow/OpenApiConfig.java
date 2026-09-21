package az.abb.embassyflow;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final Map<String, Set<HttpMethod>> PROTECTED = Map.of(
        "/api/v1/orders/{orderId}/identity", Set.of(HttpMethod.PUT),
        "/api/v1/customers/{customerId}/accounts", Set.of(HttpMethod.GET),
        "/api/v1/orders/{orderId}/items", Set.of(HttpMethod.POST),
        "/api/v1/orders/{orderId}", Set.of(HttpMethod.GET),
        "/api/v1/orders/{orderId}/preview", Set.of(HttpMethod.POST),
        "/api/v1/orders/{orderId}/pay", Set.of(HttpMethod.POST),
        "/api/v1/orders", Set.of(HttpMethod.GET)
    );

    @Bean
    public OpenApiCustomizer bearerSecurityCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.components(components);
            }
            components.addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("demo-token-customer-{id}")
                    .description("OTP yoxlanmasından (POST /auth/otp/validate) qayıdan "
                        + "accessToken-ı yapışdırın, məs. demo-token-customer-1. "
                        + "'Bearer ' prefiksi özü əlavə olunur."));
            openApi.getPaths().forEach((path, item) -> {
                Set<HttpMethod> methods = PROTECTED.get(path);
                if (methods == null) {
                    return;
                }
                item.readOperationsMap().forEach((method, op) -> {
                    if (methods.contains(method)) {
                        op.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                    }
                });
            });
        };
    }
}
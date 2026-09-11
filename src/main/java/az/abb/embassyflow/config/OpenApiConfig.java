package az.abb.embassyflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI embassyFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Embassy Flow API")
                        .version("v1.0.0")
                        .description(
                                "ABB-nin rəqəmsal arayış/sertifikat çatdırılma xidmətinin REST API-si. "
                                        + "Müştəri ABB Mobile üzərindən səfirliyə arayış sifariş edir; sənəd "
                                        + "bank tərəfindən rəqəmsal hazırlanıb imzalanır və elektron formada "
                                        + "səfirliyə çatdırılır. Base URL: /api/v1")
                        .contact(new Contact().name("ABB Card Center")));
    }
}
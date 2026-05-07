package io.cloudops.reportingservice.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reportingOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CloudOps Reporting Service API")
                .description("Dashboard analytics, métriques SLA et exports (CSV, Excel, PDF)")
                .version("2.0")
                .contact(new Contact()
                    .name("CloudOps Team")
                    .email("cloudops@company.io"))
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtenu depuis Keycloak cloudops-realm")
                )
            );
    }
}

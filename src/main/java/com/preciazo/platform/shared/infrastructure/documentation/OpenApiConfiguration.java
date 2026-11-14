package com.preciazo.platform.shared.infrastructure.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI preciazoOpenApi(@Value("${documentation.application.version}") String version) {
        return new OpenAPI().info(new Info().title("Preciazo REST API").version(version)
            .description("Preciazo REST API. Shared foundation and Shopping list creation/queries; IAM principal integration is pending."));
    }
}

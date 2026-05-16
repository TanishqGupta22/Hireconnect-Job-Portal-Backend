package com.hireconnect.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI analyticsServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireConnect Analytics Service API")
                        .description("REST API documentation for Platform Analytics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HireConnect Team")
                                .email("support@hireconnect.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}

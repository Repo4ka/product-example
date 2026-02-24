package com.repochka.product_demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productDemoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Demo API")
                        .version("1.0.0")
                        .description("REST API for managing products with CRUD operations, "
                                + "category-based discount pricing, pagination, and filtering."));
    }
}

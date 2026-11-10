package com.campus.market.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 文档信息 + 全局 Bearer 鉴权按钮（Boot 4 只能用 springdoc 3.x）。
 * 访问：http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "BearerAuth";

    @Bean
    public OpenAPI campusMarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园集市 API")
                        .version("1.0.0")
                        .description("校园二手交易平台后端接口文档"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .schemaRequirement(SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}

package com.neozeng.trackerserve.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置类
 * 配置 Swagger UI 文档信息
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("动态链接追踪平台 API 文档")
                        .description("短链接生成、管理和统计分析的 RESTful API 接口文档\n\n" +
                                "## 访问地址\n" +
                                "- **Swagger UI**: http://localhost:8080/swagger-ui.html\n" +
                                "- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml\n" +
                                "- **OpenAPI JSON**: http://localhost:8080/v3/api-docs\n\n" +
                                "## 认证说明\n" +
                                "大部分接口需要 JWT Token 认证，请在登录接口获取 Token 后，在 Swagger UI 右上角点击 \"Authorize\" 按钮，输入 `Bearer {token}` 进行认证。")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NeoZeng")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("https://api.example.com").description("生产环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入 JWT Token，格式：Bearer {token}")));
    }
}


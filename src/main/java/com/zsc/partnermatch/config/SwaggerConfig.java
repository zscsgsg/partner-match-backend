package com.zsc.partnermatch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("伙伴系统API文档")      // 文档标题
                        .description("zsc伙伴系统接口文档") // 描述
                        .version("v1.0.0"));              // 版本号
    }
    // 用户模块分组
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("用户模块")            // 分组名称
                .pathsToMatch("/user/**") // 匹配的请求路径规则
                .packagesToScan("com.zsc.partnermatch.controller") // 扫描的包
                .build();
    }

    // 用户模块分组
    @Bean
    public GroupedOpenApi publicApi1() {
        return GroupedOpenApi.builder()
                .group("标签模块")            // 分组名称
                .pathsToMatch("/tag/**") // 匹配的请求路径规则
                .packagesToScan("com.zsc.partnermatch.controller") // 扫描的包
                .build();
    }


    // 队伍模块分组
    @Bean
    public GroupedOpenApi publicApi2() {
        return GroupedOpenApi.builder()
                .group("队伍模块")            // 分组名称
                .pathsToMatch("/team/**") // 匹配的请求路径规则
                .packagesToScan("com.zsc.partnermatch.controller") // 扫描的包
                .build();
    }


}
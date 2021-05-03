package com.norway.mastercard.ibpts.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The class is used to provide swagger documentation.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket swaggerConfiguration() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.norway.mastercard.ibpts.controller"))
                .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfo(
                "Intra Bank Payment Transfer System",
                "Intra bank payment transfer system provides services to perform account operations",
                "1",
                "",
                new Contact("Praveen Palled", "", "praveen8959@gmail.com"),
                "",
                "",
                Collections.emptyList());
    }
}

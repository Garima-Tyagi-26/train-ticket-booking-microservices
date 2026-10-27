package com.algoworks.trainticketbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@OpenAPIDefinition
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig {

	 @Bean
	 public OpenAPI customOpenAPI() {
		
		 return new OpenAPI()
		            .info(new Info()
		                    .title("Train Ticket Booking Service API")
		                    .description("REST APIs for train search, ticket booking, cancellation, and booking history.")
		                    .version("1.0"))
		            .addSecurityItem(
		                    new SecurityRequirement().addList("bearerAuth")
		            );
	}
}

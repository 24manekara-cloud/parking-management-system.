package com.parking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Springdoc / Swagger UI configuration.
 * Accessible at /swagger-ui/index.html after starting the application.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI parkingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Parking Management System API")
                        .description("""
                                A production-grade RESTful API for managing parking slots, vehicle bookings,
                                billing, and daily revenue analytics.
                                
                                **Fee Policy:** ₹50 per every 3 hours (or fraction thereof).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Parking System Support")
                                .email("support@parking.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .components(new Components());
    }
}

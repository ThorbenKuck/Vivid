package com.vivid.backend.api

import com.vivid.backend.api.client.ClientIdentificationResolver
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(
    private val clientIdentificationResolver: ClientIdentificationResolver,
): WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("*")
            .allowCredentials(false)
    }

    @Bean
    fun vividOpenApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Vivid API")
                    .description("FeatureEntity Management Platform API")
                    .version("v1.0.0")
            )
    }

    override fun addArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>) {
        (resolvers as MutableList<HandlerMethodArgumentResolver>).add(clientIdentificationResolver)
    }
}
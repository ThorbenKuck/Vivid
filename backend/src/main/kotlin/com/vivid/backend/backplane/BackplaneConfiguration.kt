package com.vivid.backend.backplane

import com.vivid.backend.ApplicationProperties
import com.vivid.backend.backplane.postgres.PostgresBackplane
import com.vivid.backend.clients.streams.ClientStreams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.Executors
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger(BackplaneConfiguration::class.java)

@Configuration
@ConditionalOnProperty("application.backplane.enabled",
    havingValue = "true",
)
class BackplaneConfiguration {

    init {
        logger.info("Backplane configuration enabled")
    }

    @Bean
    @ConditionalOnEnabledBackplane("postgres")
    fun postgresBackplane(
        dataSource: DataSource,
        jdbcTemplate: JdbcTemplate,
        objectMapper: ObjectMapper,
    ): NotificationBackplane {
        return PostgresBackplane(
            dataSource = dataSource,
            jdbcTemplate = jdbcTemplate,
            objectMapper = objectMapper
        )
    }

    @Bean
    fun notificationBackplaneManager(
        backplanes: List<NotificationBackplane>,
        clientStreams: ObjectProvider<ClientStreams>,
        applicationProperties: ApplicationProperties,
    ): NotificationBackplaneManager? {
        val clientStreams = clientStreams.getIfAvailable() ?: return null
        if (backplanes.isEmpty()) {
            logger.info("No notification backplanes configured")
            return null
        }

        logger.info("Initializing notification backplane manager with {} backplanes: {}", backplanes.size, backplanes)
        return NotificationBackplaneManager(
            backplanes = backplanes,
            executor = Executors.newFixedThreadPool(backplanes.size),
            clientStreams = clientStreams,
            applicationProperties = applicationProperties
        )
    }
}

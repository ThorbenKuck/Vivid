package com.vivid.backend

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.UUID.randomUUID

@ConfigurationProperties("application")
class ApplicationProperties(
    val instanceId: String? = randomUUID().toString(),
    val database: DB,
    val oidc: OIDC = OIDC(),
    val clients: Clients = Clients(),
    val backplane: Backplane = Backplane(),
    val actuator: Actuator = Actuator(),
) {
    data class Backplane(
        val enabled: Boolean = false,
        val postgres: Postgres = Postgres()
    ) {
        data class Postgres(
            val enabled: Boolean = false,
        )
    }

    data class Actuator(
        val enabled: Boolean = true,
        val secured: Boolean = false,
    )

    data class OIDC(
        val issuerUrl: String? = null,
        val issuerName: String? = null,
        val clientId: String? = null,
        val frontendBaseUrl: String? = null,
        val logoutUrl: String? = null,
    ) {
        fun calculateLogoutUrl(): String? {
            if (logoutUrl != null) {
                return logoutUrl
            }

            return if (!clientId.isNullOrBlank() && !issuerUrl.isNullOrBlank()) {
                "$issuerUrl/protocol/openid-connect/logout?client_id=$clientId&post_logout_redirect_uri=$frontendBaseUrl/login"
            } else {
                null
            }
        }
    }

    data class DB(
        val url: String,
        val username: String,
        val password: String,
    )

    data class Clients(
        val streams: Streams = Streams(),
    ) {
        data class Streams(
            val enabled: Boolean = true,
            val sse: SSE = SSE(),
        ) {
            data class SSE(
                val enabled: Boolean = true,
            )
        }
    }
}

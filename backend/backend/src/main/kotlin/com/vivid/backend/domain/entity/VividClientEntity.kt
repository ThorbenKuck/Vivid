package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity(name = "VividClient")
@Table(
    name = "vivid_clients",
    uniqueConstraints = [UniqueConstraint(columnNames = ["external_id", "environment_id"])]
)
class VividClientEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "client_name", nullable = false)
    var clientName: String,

    /**
     * A token assigned to this client.
     *
     * Tokens are optional and only required if Vivid is configured with static client tokens.
     * In this configuration, each client needs to be registered before it can be used.
     * During registration, a client token is generated and assigned to the client.
     *
     * Optionally, you can configure
     */
    @Column(name = "client_token", nullable = true)
    var clientToken: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    var environment: EnvironmentEntity,

    /**
     * When the client was last seen.
     *
     * Null if the client has never been seen.
     * This can be the case if the client was registered but not yet used.
     */
    @Column(name = "last_seen", nullable = true)
    var lastSeen: Instant? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vivid_client_technologies", joinColumns = [JoinColumn(name = "client_id")])
    @Column(name = "technology")
    var technologies: MutableSet<String> = mutableSetOf(),

    @Column(name = "client_version", nullable = true)
    var clientVersion: String? = null
) {
    fun updateBy(entity: VividClientEntity): VividClientEntity {
        lastSeen = Instant.now()

        clientName = entity.clientName
        technologies = entity.technologies.toMutableSet()
        clientVersion = entity.clientVersion ?: clientVersion

        return this
    }
}

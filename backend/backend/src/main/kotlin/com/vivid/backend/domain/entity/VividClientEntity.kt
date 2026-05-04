package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity(name = "VividClient")
@Table(
    name = "vivid_clients",
    uniqueConstraints = [UniqueConstraint(columnNames = ["client_name"])]
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
     */
    @Column(name = "client_token", nullable = true)
    var clientToken: String?,

    @OneToMany(
        mappedBy = "client",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    var presences: MutableList<VividClientPresenceEntity> = mutableListOf()
) {
    fun updateBy(entity: VividClientEntity): VividClientEntity {
        clientName = entity.clientName
        return this
    }
}

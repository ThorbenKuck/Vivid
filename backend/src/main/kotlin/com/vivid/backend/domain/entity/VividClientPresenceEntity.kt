package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity(name = "VividClientPresence")
@Table(
    name = "vivid_client_presences",
    uniqueConstraints = [UniqueConstraint(columnNames = ["client_id", "environment_id"])]
)
class VividClientPresenceEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    var client: VividClientEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "environment_id", nullable = false)
    var environment: EnvironmentEntity,

    @Column(name = "last_seen", nullable = false)
    var lastSeen: Instant = Instant.now(),

    /**
     * The technologies used by the client to receive push notifications about features.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vivid_client_presence_technologies", joinColumns = [JoinColumn(name = "presence_id")])
    @Column(name = "technology")
    var technologies: MutableSet<String> = mutableSetOf(),

    @Column(name = "client_version", nullable = true)
    var clientVersion: String? = null
)

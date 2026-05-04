package com.vivid.backend.domain.entity.infrastructure

import com.vivid.backend.domain.entity.VividClientEntity
import jakarta.persistence.*
import java.time.Instant
import java.io.Serializable
import java.util.UUID

@Embeddable
data class FeatureUsageId(
    @Column(name = "feature_id")
    val featureId: UUID,
    @Column(name = "client_id")
    val clientId: UUID
) : Serializable

@Entity(name = "FeatureUsage")
@Table(name = "feature_usages")
class FeatureUsageEntity(
    @EmbeddedId
    var id: FeatureUsageId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("featureId")
    @JoinColumn(name = "feature_id")
    var feature: FeatureEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("clientId")
    @JoinColumn(name = "client_id")
    var client: VividClientEntity,

    @Column(name = "last_seen", nullable = false)
    var lastSeen: Instant = Instant.now()
)

package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "feature_links")
class FeatureLink(
    id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_feature_id", nullable = false)
    var sourceFeature: Feature,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_feature_id", nullable = false)
    var targetFeature: Feature,

    @Column(name = "link_type")
    var type: String? = null
): BaseUuidEntity(id)

package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "notes")
class Note(
    id: UUID = UUID.randomUUID(),

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @Column(nullable = false)
    var timestamp: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", nullable = false)
    var feature: FeatureEntity
) : BaseUuidEntity(id)

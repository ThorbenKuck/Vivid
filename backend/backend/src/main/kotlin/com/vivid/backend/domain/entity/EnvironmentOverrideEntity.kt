package com.vivid.backend.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity(name = "EnvironmentOverride")
@Table(
    name = "environment_overrides",
    uniqueConstraints = [UniqueConstraint(columnNames = ["feature_id", "environment_id"])]
)
class EnvironmentOverrideEntity(
    id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    val feature: FeatureEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "environment_id", nullable = false)
    val environment: EnvironmentEntity,

    @Column
    var enabled: Boolean? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "environment_override_flags", joinColumns = [JoinColumn(name = "override_id")])
    @MapKeyColumn(name = "flag_key")
    @Column(name = "flag_value")
    var flags: MutableMap<String, Boolean> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Map<String, MetadataValue> = emptyMap(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var strategy: OverrideStrategy = OverrideStrategy.OVERRIDE
) : BaseUuidEntity(id)

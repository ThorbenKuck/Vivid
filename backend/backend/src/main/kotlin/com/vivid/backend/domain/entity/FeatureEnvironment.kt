package com.vivid.backend.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(
    name = "feature_environments",
    uniqueConstraints = [UniqueConstraint(columnNames = ["feature_id", "environment_id"])]
)
class FeatureEnvironment(
    id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", nullable = false)
    val feature: Feature,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "environment_id", nullable = false)
    val environment: Environment,

    @Column(nullable = false)
    var enabled: Boolean = false,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "feature_environment_flags", joinColumns = [JoinColumn(name = "feature_environment_id")])
    @MapKeyColumn(name = "flag_key", nullable = false)
    @Column(name = "flag_value", nullable = false)
    var flags: MutableMap<String, Boolean> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Map<String, MetadataValue> = emptyMap()
): BaseUuidEntity(id)

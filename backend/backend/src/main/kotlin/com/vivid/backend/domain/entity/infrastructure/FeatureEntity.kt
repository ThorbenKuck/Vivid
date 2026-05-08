package com.vivid.backend.domain.entity.infrastructure

import com.vivid.backend.domain.entity.BaseUuidEntity
import com.vivid.backend.domain.entity.EnvironmentOverrideEntity
import com.vivid.backend.domain.entity.FeatureLink
import com.vivid.backend.domain.entity.MetadataValue
import com.vivid.backend.domain.entity.Note
import com.vivid.backend.domain.entity.OverrideStrategy
import com.vivid.backend.domain.entity.StringListMetadataValue
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity(name = "Feature")
@Table(name = "features")
class FeatureEntity(
    id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(name = "\"key\"", nullable = false, unique = true)
    var key: String,

    @Column(name = "running_number", nullable = false, unique = true, updatable = false)
    var runningNumber: Long,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    var enabled: Boolean = false,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "feature_flags", joinColumns = [JoinColumn(name = "feature_id")])
    @MapKeyColumn(name = "flag_key")
    @Column(name = "flag_value")
    var flags: MutableMap<String, Boolean> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Map<String, MetadataValue> = emptyMap(),

    @OneToMany(
        mappedBy = "feature",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    var environmentOverrides: MutableList<EnvironmentOverrideEntity> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "feature_tags", joinColumns = [JoinColumn(name = "feature_id")])
    @Column(name = "tag", nullable = false)
    var tags: MutableSet<String> = mutableSetOf(),

    @OneToMany(mappedBy = "sourceFeature", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var outgoingLinks: MutableList<FeatureLink> = mutableListOf(),

    @OneToMany(mappedBy = "targetFeature", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var incomingLinks: MutableList<FeatureLink> = mutableListOf(),

    @OneToMany(mappedBy = "feature", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("timestamp DESC")
    var notes: MutableList<Note> = mutableListOf()
) : BaseUuidEntity(id) {
    fun resolve(environmentId: UUID?): ResolvedFeatureState {
        if (environmentId == null) {
            return ResolvedFeatureState(enabled, flags, metadata)
        }
        val override = environmentOverrides.find { it.environment.id == environmentId }
            ?: return ResolvedFeatureState(enabled, flags, metadata ?: emptyMap())

        return when (override.strategy) {
            OverrideStrategy.OVERRIDE -> ResolvedFeatureState(
                enabled = override.enabled ?: enabled,
                flags = override.flags.toMap(),
                metadata = override.metadata.toMap()
            )

            OverrideStrategy.EXTEND -> {
                val resolvedFlags = flags.toMutableMap()
                resolvedFlags.putAll(override.flags)

                val resolvedMetadata = metadata.toMutableMap()
                override.metadata.forEach { (key, value) ->
                    val existing = resolvedMetadata[key]
                    if (existing is StringListMetadataValue && value is StringListMetadataValue) {
                        resolvedMetadata[key] = StringListMetadataValue(existing.content + value.content)
                    } else {
                        resolvedMetadata[key] = value
                    }
                }

                ResolvedFeatureState(
                    enabled = override.enabled ?: enabled,
                    flags = resolvedFlags,
                    metadata = resolvedMetadata
                )
            }
        }
    }

    data class ResolvedFeatureState(
        val enabled: Boolean,
        val flags: Map<String, Boolean>,
        val metadata: Map<String, MetadataValue>
    )
}
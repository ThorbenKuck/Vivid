package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "features")
class Feature(
    id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false, unique = true)
    var key: String,

    @Column(name = "running_number", nullable = false, unique = true, updatable = false)
    var runningNumber: Long,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @OneToMany(mappedBy = "feature", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var environments: MutableList<FeatureEnvironment> = mutableListOf(),

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
): BaseUuidEntity(id) {
    fun findFeatureEnvironment(environment: EnvironmentEntity): FeatureEnvironment? {
        return environments.find { it.environment == environment }
    }
}

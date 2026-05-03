package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.util.*

@Entity(name = "Environment")
@Table(name = "environments")
class EnvironmentEntity(
    id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(name = "\"key\"", nullable = false, unique = true)
    var key: String,

    @Column(nullable = false)
    var weight: Int? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null
): BaseUuidEntity(id)

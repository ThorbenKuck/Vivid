package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "environments")
class Environment(
    id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null
): BaseUuidEntity(id)

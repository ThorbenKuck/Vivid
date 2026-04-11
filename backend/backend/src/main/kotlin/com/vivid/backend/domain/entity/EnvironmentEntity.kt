package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.util.*

@Entity(name = "Environment")
@Table(name = "environments")
class EnvironmentEntity(
    id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    var department: Department
): BaseUuidEntity(id)

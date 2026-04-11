package com.vivid.backend.domain.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "vivid_users")
class User(
    id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var keycloakId: String,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = true)
    var email: String?,

    @Column(name = "display_role")
    var displayRole: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    var department: Department
) : BaseUuidEntity(id)

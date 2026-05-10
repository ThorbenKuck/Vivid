package com.vivid.backend.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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
    var sortOrder: Int = 0,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    var rules: List<EnvironmentRule> = emptyList(),

    @Column(columnDefinition = "TEXT")
    var description: String? = null
): BaseUuidEntity(id) {
    override fun toString(): String {
        return "Environment($name)"
    }
}

data class EnvironmentRule(
    val type: String = "",
    val config: Map<String, Any?> = emptyMap()
)

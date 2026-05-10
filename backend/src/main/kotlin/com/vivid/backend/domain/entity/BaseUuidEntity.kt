package com.vivid.backend.domain.entity

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.*

@MappedSuperclass
abstract class BaseUuidEntity(
    @Id
    open var id: UUID = UUID.randomUUID()
) {

    override fun equals(other: Any?): Boolean {
        return other is BaseUuidEntity && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

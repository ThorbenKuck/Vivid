package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.VividClientEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface VividClientRepository : JpaRepository<VividClientEntity, UUID> {
    fun findByClientNameAndEnvironment(externalId: String, environment: EnvironmentEntity): VividClientEntity?
    fun findByClientTokenAndEnvironment(externalId: String, environment: EnvironmentEntity): VividClientEntity?
    fun findAllByEnvironmentId(environmentId: UUID): List<VividClientEntity>
}

package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.VividClientEntity
import com.vivid.backend.domain.entity.VividClientPresenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface VividClientPresenceRepository : JpaRepository<VividClientPresenceEntity, UUID> {
    fun findByClientAndEnvironment(client: VividClientEntity, environment: EnvironmentEntity): VividClientPresenceEntity?
    fun findAllByEnvironmentId(environmentId: UUID): List<VividClientPresenceEntity>
}

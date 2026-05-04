package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.VividClientEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface VividClientRepository : JpaRepository<VividClientEntity, UUID> {
    fun findByClientName(clientName: String): VividClientEntity?
    fun findByClientToken(clientToken: String): VividClientEntity?

    @Query("SELECT c FROM VividClient c LEFT JOIN FETCH c.presences")
    fun findAllWithPresences(): List<VividClientEntity>
}

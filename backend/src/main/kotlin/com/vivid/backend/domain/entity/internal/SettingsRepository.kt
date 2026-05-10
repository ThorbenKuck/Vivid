package com.vivid.backend.domain.entity.internal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface SettingsRepository : JpaRepository<SettingsEntity, UUID> {

    @Query("SELECT s FROM Settings s WHERE s.active = true ORDER BY s.lastUpdated DESC FETCH FIRST 1 ROWS ONLY")
    fun findSettingsEntity(): SettingsEntity?
}

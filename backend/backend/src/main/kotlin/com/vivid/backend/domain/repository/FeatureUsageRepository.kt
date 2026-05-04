package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.infrastructure.FeatureUsageEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureUsageId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeatureUsageRepository : JpaRepository<FeatureUsageEntity, FeatureUsageId> {
    fun findAllByFeatureId(featureId: UUID): List<FeatureUsageEntity>
}

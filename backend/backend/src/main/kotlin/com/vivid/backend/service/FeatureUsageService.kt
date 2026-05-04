package com.vivid.backend.service

import com.vivid.backend.domain.entity.VividClientEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureUsageEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureUsageId
import com.vivid.backend.domain.repository.FeatureUsageRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class FeatureUsageService(
    private val repository: FeatureUsageRepository
) {

    @Async
    @Transactional
    fun recordUsage(feature: FeatureEntity, client: VividClientEntity) {
        val id = FeatureUsageId(feature.id, client.id)
        val usage = repository.findById(id).orElseGet {
            FeatureUsageEntity(id, feature, client)
        }
        usage.lastSeen = Instant.now()
        repository.save(usage)
    }

    @Transactional(readOnly = true)
    fun getUsageForFeature(featureId: UUID): List<FeatureUsageEntity> {
        return repository.findAllByFeatureId(featureId)
    }
}

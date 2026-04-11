package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.FeatureEnvironment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface FeatureEnvironmentRepository : JpaRepository<FeatureEnvironment, UUID> {

    fun findByFeatureIdAndEnvironment(featureId: UUID, environment: EnvironmentEntity): FeatureEnvironment?

    fun findAllByEnvironmentAndEnabledTrue(environment: EnvironmentEntity): List<FeatureEnvironment>

    fun findAllByFeatureIdInAndEnvironment(featureIds: Collection<UUID>, environment: EnvironmentEntity): List<FeatureEnvironment>

    @Query(
        """
        select distinct fe.feature.id from FeatureEnvironment fe
        where fe.environment.id = :environmentId and fe.enabled = true
        """
    )
    fun findEnabledFeatureIds(environmentId: UUID): List<UUID>
}
package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.Feature
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface FeatureRepository : JpaRepository<Feature, UUID> {
    fun findByName(name: String): Feature?
    fun findByRunningNumber(runningNumber: Long): Optional<Feature>
    fun findByRunningNumberAndDepartmentId(runningNumber: Long, departmentId: UUID): Optional<Feature>

    @Query(
        """
    select distinct f from Feature f
    left join f.tags t
    where f.department.id = :departmentId
    and (:q is null or :q = '' or lower(f.name) like lower(concat('%', :q, '%'))
           or lower(coalesce(f.description, '')) like lower(concat('%', :q, '%'))
           or lower(t) like lower(concat('%', :q, '%')))
        """
    )
    fun search(q: String?, departmentId: UUID, pageable: Pageable): Page<Feature>

    @Query("select distinct t from Feature f join f.tags t where f.department.id = :departmentId")
    fun findAllDistinctTags(departmentId: UUID): List<String>

    fun findAllByDepartmentId(departmentId: UUID, pageable: Pageable): Page<Feature>

    @Query(value = "SELECT nextval('feature_running_number_seq')", nativeQuery = true)
    fun getNextRunningNumber(): Long
}

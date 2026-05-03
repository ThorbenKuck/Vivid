package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.Feature
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface FeatureRepository : JpaRepository<Feature, UUID> {
    fun findByName(name: String): Feature?
    fun findByRunningNumber(runningNumber: Long): Feature?

    @Query(
        """
    select distinct f from Feature f
    left join f.tags t
    where (:q is null or :q = '' or lower(f.name) like lower(concat('%', :q, '%'))
           or lower(coalesce(f.description, '')) like lower(concat('%', :q, '%'))
           or lower(t) like lower(concat('%', :q, '%')))
        """
    )
    fun search(q: String?, pageable: Pageable): Page<Feature>

    @Query("select distinct t from Feature f join f.tags t")
    fun findAllDistinctTags(): List<String>

    @Query(value = "SELECT nextval('feature_running_number_seq')", nativeQuery = true)
    fun getNextRunningNumber(): Long

    @Query("select f from Feature f where f.key = :key")
    fun findByKey(key: String): Feature?
}

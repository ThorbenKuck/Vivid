package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface EnvironmentRepository : JpaRepository<EnvironmentEntity, UUID> {

    fun existsByNameIgnoreCase(name: String): Boolean

    @Query(
        """
        select e from Environment e
        where (:q is null or :q = '' or lower(e.name) like lower(concat('%', :q, '%')))
        """
    )
    fun search(q: String?, pageable: Pageable): Page<EnvironmentEntity>

    @Query("select e from Environment e where e.key = :key")
    fun findByKey(key: String): EnvironmentEntity?

    fun findByName(name: String): EnvironmentEntity?
}
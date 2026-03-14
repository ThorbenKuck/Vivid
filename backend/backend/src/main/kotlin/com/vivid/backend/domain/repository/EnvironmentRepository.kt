package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface EnvironmentRepository : JpaRepository<Environment, UUID> {

    fun existsByNameIgnoreCase(name: String): Boolean

    @Query(
        """
        select e from Environment e
        where (:q is null or :q = '' or lower(e.name) like lower(concat('%', :q, '%')))
        """
    )
    fun search(q: String?, pageable: Pageable): Page<Environment>

    fun findByName(name: String): Environment?
}
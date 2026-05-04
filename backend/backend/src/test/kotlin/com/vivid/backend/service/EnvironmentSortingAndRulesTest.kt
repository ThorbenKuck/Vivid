package com.vivid.backend.service

import com.vivid.backend.api.web.dto.EnvironmentCreateRequest
import com.vivid.backend.api.web.dto.EnvironmentRuleDto
import com.vivid.backend.api.web.dto.EnvironmentUpdateRequest
import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.repository.EnvironmentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnvironmentSortingAndRulesTest {

    @Autowired
    lateinit var environmentService: EnvironmentService

    @Autowired
    lateinit var environmentRepository: EnvironmentRepository

    @Test
    fun `should create environment with incremented sort order`() {
        val env1 = environmentService.create(EnvironmentCreateRequest("Env 1", "Desc 1"))
        val env2 = environmentService.create(EnvironmentCreateRequest("Env 2", "Desc 2"))

        assertTrue(env2.sortOrder > env1.sortOrder)
    }

    @Test
    fun `should reorder environments`() {
        val env1 = environmentService.create(EnvironmentCreateRequest("A", "Desc"))
        val env2 = environmentService.create(EnvironmentCreateRequest("B", "Desc"))
        
        environmentService.reorder(listOf(env2.id, env1.id))
        
        val sorted = environmentService.getAll()
        assertEquals("B", sorted[0].name)
        assertEquals("A", sorted[1].name)
        assertEquals(0, sorted[0].sortOrder)
        assertEquals(1, sorted[1].sortOrder)
    }

    @Test
    fun `should update rules`() {
        val env = environmentService.create(EnvironmentCreateRequest("Rules Env", "Desc"))
        val rule = EnvironmentRuleDto("MATCH_ENVIRONMENT", mapOf("sourceEnvironmentId" to UUID.randomUUID().toString()))
        
        val updated = environmentService.update(env.id, EnvironmentUpdateRequest(rules = listOf(rule)))
        
        assertEquals(1, updated.rules.size)
        assertEquals("MATCH_ENVIRONMENT", updated.rules[0].type)
        assertEquals(rule.config["sourceEnvironmentId"], updated.rules[0].config["sourceEnvironmentId"])
    }
}

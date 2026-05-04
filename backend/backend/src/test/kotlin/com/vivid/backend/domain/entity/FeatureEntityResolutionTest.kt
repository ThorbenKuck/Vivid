package com.vivid.backend.domain.entity

import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class FeatureEntityResolutionTest {

    @Test
    fun `should return global defaults when no environment provided`() {
        val feature = FeatureEntity(
            name = "test",
            key = "test",
            runningNumber = 1,
            enabled = true,
            flags = mutableMapOf("f1" to true),
            metadata = mapOf("m1" to StringListMetadataValue(listOf("v1")))
        )

        val resolved = feature.resolve(null)

        assertTrue(resolved.enabled)
        assertEquals(true, resolved.flags["f1"])
        assertEquals(listOf("v1"), (resolved.metadata["m1"] as StringListMetadataValue).content)
    }

    @Test
    fun `should return global defaults when no override found`() {
        val feature = FeatureEntity(
            name = "test",
            key = "test",
            runningNumber = 1,
            enabled = true
        )
        val envId = UUID.randomUUID()

        val resolved = feature.resolve(envId)

        assertTrue(resolved.enabled)
    }

    @Test
    fun `should apply override strategy`() {
        val feature = FeatureEntity(
            name = "test",
            key = "test",
            runningNumber = 1,
            enabled = true,
            flags = mutableMapOf("f1" to true, "f2" to false)
        )
        val env = EnvironmentEntity(name = "prod", key = "prod")
        val override = EnvironmentOverrideEntity(
            feature = feature,
            environment = env,
            enabled = false,
            flags = mutableMapOf("f1" to false),
            strategy = OverrideStrategy.OVERRIDE
        )
        feature.environmentOverrides.add(override)

        val resolved = feature.resolve(env.id)

        assertFalse(resolved.enabled)
        assertEquals(false, resolved.flags["f1"])
        assertNull(resolved.flags["f2"]) // OVERRIDE replaces the whole map
    }

    @Test
    fun `should apply extend strategy`() {
        val feature = FeatureEntity(
            name = "test",
            key = "test",
            runningNumber = 1,
            enabled = true,
            flags = mutableMapOf("f1" to true, "f2" to false),
            metadata = mapOf("m1" to StringListMetadataValue(listOf("v1")))
        )
        val env = EnvironmentEntity(name = "prod", key = "prod")
        val override = EnvironmentOverrideEntity(
            feature = feature,
            environment = env,
            enabled = null, // Inherit
            flags = mutableMapOf("f1" to false, "f3" to true),
            metadata = mapOf("m1" to StringListMetadataValue(listOf("v2")), "m2" to LongMetadataValue(42)),
            strategy = OverrideStrategy.EXTEND
        )
        feature.environmentOverrides.add(override)

        val resolved = feature.resolve(env.id)

        assertTrue(resolved.enabled) // Inherited
        assertEquals(false, resolved.flags["f1"]) // Overridden
        assertEquals(false, resolved.flags["f2"]) // Kept
        assertEquals(true, resolved.flags["f3"]) // Added
        
        val m1 = resolved.metadata["m1"] as StringListMetadataValue
        assertEquals(listOf("v1", "v2"), m1.content) // Appended
        
        val m2 = resolved.metadata["m2"] as LongMetadataValue
        assertEquals(42L, m2.content) // Added
    }
}

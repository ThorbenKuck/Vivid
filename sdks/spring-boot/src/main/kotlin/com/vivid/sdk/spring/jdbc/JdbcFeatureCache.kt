package com.vivid.sdk.spring.jdbc

import com.vivid.sdk.FeatureCache
import com.vivid.sdk.Subscription
import com.vivid.sdk.api.Feature
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.ObjectMapper
import java.sql.Timestamp
import java.util.concurrent.ConcurrentHashMap

class JdbcFeatureCache(
    private val jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
    private val objectMapper: ObjectMapper,
    private val missHandler: (String) -> Feature = { Feature.empty() }
) : FeatureCache {

    private var enabled: Boolean = true
    private val subscriptions = ConcurrentHashMap<String, MutableList<FeatureCache.Callback>>()

    override fun get(key: String): Feature? {
        if (!enabled) return null

        return transactionTemplate.execute {
            // 1. Resolve id through alias
            val featureId = findIdByAlias(key) ?: key

            // 2. Fetch feature from DB
            val feature = loadFeature(featureId)

            if (feature != null && !feature.isEmpty()) {
                feature
            } else {
                // 3. Miss-Handling: Feature holen, speichern und Alias setzen
                val match = missHandler(key)
                if (!match.isEmpty()) {
                    saveInternal(match)
                    setAlias(key, match)
                }
                match.nullIfEmpty()
            }
        }
    }

    override fun set(feature: Feature): Feature? {
        if (!enabled) return null
        return transactionTemplate.execute {
            val previous = loadFeature(feature.id)
            if (previous == null || feature.timestamp.isAfter(previous.timestamp)) {
                saveInternal(feature)
                notifySubscribers(feature, isRemoval = false)
            }
            previous
        }
    }

    override fun setAll(features: List<Feature>) {
        if (!enabled) return
        transactionTemplate.executeWithoutResult {
            val newIds = features.map { it.id }

            // Upsert für alle neuen Features
            features.forEach { set(it) }

            // Evict für alle, die nicht mehr in der Liste sind
            val existingIds = jdbcTemplate.queryForList("SELECT id FROM features", String::class.java).filterNotNull()

            existingIds.filter { it !in newIds }.forEach { id ->
                invalidate(id)
            }
        }
    }

    override fun setAlias(alias: String, feature: Feature) {
        val sql = """
            INSERT INTO feature_aliases (alias_name, feature_id) VALUES (?, ?)
            ON CONFLICT (alias_name) DO UPDATE SET feature_id = EXCLUDED.feature_id
        """.trimIndent()
        jdbcTemplate.update(sql, alias, feature.id)
    }

    override fun invalidate(key: String): Feature? {
        return transactionTemplate.execute {
            val featureId = findIdByAlias(key) ?: key
            val previous = loadFeature(featureId)

            if (previous != null) {
                jdbcTemplate.update("DELETE FROM features WHERE id = ?", featureId)
                notifySubscribers(previous, isRemoval = true)
            }
            previous
        }
    }

    override fun invalidate() {
        transactionTemplate.executeWithoutResult {
            jdbcTemplate.update("DELETE FROM features") // Löscht alles inklusive Aliases
        }
    }

    override fun getAll(): List<Feature> {
        return jdbcTemplate.query("SELECT payload FROM features") { rs, _ ->
            deserialize(rs.getString("payload"))
        }.filter { !it.isEmpty() }
    }

    override fun enable(state: Boolean) {
        this.enabled = state
    }

    // --- Private Helper ---

    private fun saveInternal(feature: Feature) {
        val json = objectMapper.writeValueAsString(feature)
        val sql = """
            INSERT INTO features (id, payload, version_timestamp) VALUES (?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET 
                payload = EXCLUDED.payload, 
                version_timestamp = EXCLUDED.version_timestamp
        """.trimIndent()
        jdbcTemplate.update(sql, feature.id, json, Timestamp.from(feature.timestamp))
    }

    private fun loadFeature(id: String): Feature? {
        return try {
            jdbcTemplate.queryForObject("SELECT payload FROM features WHERE id = ?", { rs, _ ->
                deserialize(rs.getString("payload"))
            }, id)
        } catch (_: Exception) {
            null
        }
    }

    private fun findIdByAlias(alias: String): String? {
        return try {
            jdbcTemplate.queryForObject("SELECT feature_id FROM feature_aliases WHERE alias_name = ?",
                String::class.java, alias)
        } catch (_: Exception) {
            null
        }
    }

    private fun deserialize(json: String): Feature = objectMapper.readValue(json, Feature::class.java)

    private fun notifySubscribers(feature: Feature, isRemoval: Boolean) {
        val callbacks = subscriptions[feature.name] ?: return
        callbacks.forEach { if (isRemoval) it.onRemove(feature) else it.onNext(feature) }
    }

    override fun subscribe(key: String, callback: FeatureCache.Callback): Subscription {
        subscriptions.computeIfAbsent(key) { mutableListOf() }.add(callback)
        return object : Subscription {
            override fun cancel() {
                subscriptions[key]?.remove(callback)
            }
        }
    }
}
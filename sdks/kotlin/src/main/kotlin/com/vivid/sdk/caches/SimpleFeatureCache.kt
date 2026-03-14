package com.vivid.sdk.caches

import com.vivid.sdk.api.Feature
import java.util.concurrent.ConcurrentHashMap

class SimpleFeatureCache : BaseFeatureCache() {

    private val content = ConcurrentHashMap<String, Feature>()

    override fun get(key: String): Feature? {
        return content[key]
    }

    private val writeLock = Any()

    override fun set(feature: Feature): Feature? {
        // Diese Methode bleibt für Einzel-Updates erhalten,
        // nutzt aber intern die neue Logik ohne sofortiges Blockieren,
        // falls wir sie von setAll aus aufrufen wollen.
        return synchronized(writeLock) {
            val (previous, updatedFeature) = updateIfNewer(feature)
            if (updatedFeature != null) {
                notifySubscribersAboutNext(updatedFeature)
            }
            previous
        }
    }

    override fun setAll(features: List<Feature>) {
        val newFeaturesMap = features.associateBy { it.name }
        val newKeys = newFeaturesMap.keys
        val updatedForNotification = mutableListOf<Feature>()

        synchronized(writeLock) {
            // 1. Alle Features prüfen und Map aktualisieren
            features.forEach { incoming ->
                val (_, updated) = updateIfNewer(incoming)
                if (updated != null) {
                    updatedForNotification.add(updated)
                }
            }

            // 2. Cleanup veralteter Keys
            content.keys.removeIf { it !in newKeys }
        }

        // 3. Erst JETZT (außerhalb des Locks!) die Subscriber benachrichtigen
        // Wenn ein Subscriber hier blockiert, sind die Daten in der Map
        // trotzdem schon für alle anderen Leser verfügbar.
        updatedForNotification.forEach {
            notifySubscribersAboutNext(it)
        }
    }

    /**
     * Hilfsfunktion für atomares Update pro Key.
     * Gibt das alte Feature und das neue (falls aktualisiert) zurück.
     */
    private fun updateIfNewer(feature: Feature): Pair<Feature?, Feature?> {
        var previous: Feature? = null
        var updated: Feature? = null

        content.compute(feature.name) { _, existing ->
            if (existing == null || feature.timestamp.isAfter(existing.timestamp)) {
                previous = existing
                updated = feature
                feature
            } else {
                updated = null
                existing
            }
        }
        return previous to updated
    }
    override fun invalidate(key: String): Feature? {
        return content.remove(key)?.also { notifySubscribersAboutRemove(it) }
    }
}
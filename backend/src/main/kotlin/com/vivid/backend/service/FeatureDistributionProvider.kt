package com.vivid.backend.service

import com.vivid.backend.clients.streams.dto.FeatureChangedEvent

interface FeatureDistributionProvider {
    fun onFeatureChanged(event: FeatureChangedEvent)
}

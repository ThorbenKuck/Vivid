package com.vivid.backend.service

import com.vivid.backend.domain.event.FeatureChangedEvent

interface FeatureDistributionProvider {
    fun onFeatureChanged(event: FeatureChangedEvent)
}

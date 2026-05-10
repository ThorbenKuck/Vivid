package com.vivid.backend.clients.streams

import com.vivid.backend.clients.streams.dto.FeatureChangedEvent

interface ClientStream {

    fun start() {}

    fun shutdown() {}

    fun push(event: FeatureChangedEvent)

}

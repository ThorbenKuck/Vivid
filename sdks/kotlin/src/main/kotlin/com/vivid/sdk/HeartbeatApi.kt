package com.vivid.sdk

import com.vivid.sdk.api.Heartbeat

interface HeartbeatApi {

    fun sendHeartbeat()

    fun sendHeartbeat(heartbeat: Heartbeat)

}

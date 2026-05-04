package com.vivid.sdk.api

data class Heartbeat(
    val clientToken: String?,
    val applicationName: String,
    val environment: String,
    val technologies: Set<String>,
    val clientVersion: String?,
)

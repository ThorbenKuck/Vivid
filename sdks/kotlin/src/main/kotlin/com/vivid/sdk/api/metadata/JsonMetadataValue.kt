package com.vivid.sdk.api.metadata

import tools.jackson.databind.JsonNode

import com.vivid.sdk.api.MetadataValue

data class JsonMetadataValue(
    val content: JsonNode
) : MetadataValue

package com.vivid.backend.domain.entity

import com.fasterxml.jackson.databind.JsonNode

data class JsonMetadataValue(
    val content: JsonNode
) : MetadataValue()

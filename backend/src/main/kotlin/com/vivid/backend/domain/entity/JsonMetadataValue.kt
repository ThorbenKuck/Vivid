package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.JsonNode

data class JsonMetadataValue(
    @JsonProperty("content")
    override val content: JsonNode
) : MetadataValue<JsonNode>

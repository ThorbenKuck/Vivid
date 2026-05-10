package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class StringMetadataValue(
    @JsonProperty("content")
    override val content: String
) : MetadataValue<String>

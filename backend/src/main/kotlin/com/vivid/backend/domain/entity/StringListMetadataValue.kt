package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class StringListMetadataValue(
    @JsonProperty("content")
    override val content: List<String>
) : MetadataValue<List<String>>

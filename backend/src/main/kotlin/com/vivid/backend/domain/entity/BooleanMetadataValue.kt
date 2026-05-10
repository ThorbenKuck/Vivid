package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class BooleanMetadataValue(
    @JsonProperty("content")
    override val content: Boolean
) : MetadataValue<Boolean>

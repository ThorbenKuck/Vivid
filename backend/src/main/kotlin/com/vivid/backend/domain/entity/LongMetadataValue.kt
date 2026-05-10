package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class LongMetadataValue(
    @JsonProperty("content")
    override val content: Long
) : MetadataValue<Long>

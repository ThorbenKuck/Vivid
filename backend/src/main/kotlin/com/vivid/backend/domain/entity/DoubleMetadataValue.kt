package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class DoubleMetadataValue(
    @JsonProperty("content")
    override val content: Double
) : MetadataValue<Double>

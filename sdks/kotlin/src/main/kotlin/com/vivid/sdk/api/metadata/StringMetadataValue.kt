package com.vivid.sdk.api.metadata

import com.vivid.sdk.api.MetadataValue

/**
 * Metadata value containing a string.
 *
 * @property content the string value
 */
data class StringMetadataValue(
    val content: String
) : MetadataValue

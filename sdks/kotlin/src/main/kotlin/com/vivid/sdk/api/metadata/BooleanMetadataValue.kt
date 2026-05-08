package com.vivid.sdk.api.metadata

import com.vivid.sdk.api.MetadataValue

/**
 * Metadata value containing a boolean.
 *
 * @property content the boolean value
 */
data class BooleanMetadataValue(
    val content: Boolean
) : MetadataValue

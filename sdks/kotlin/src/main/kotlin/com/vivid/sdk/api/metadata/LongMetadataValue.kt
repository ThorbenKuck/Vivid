package com.vivid.sdk.api.metadata

import com.vivid.sdk.api.MetadataValue

/**
 * Metadata value containing a long.
 *
 * @property content the long value
 */
data class LongMetadataValue(
    val content: Long
) : MetadataValue

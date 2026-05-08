package com.vivid.sdk.api.metadata

import com.vivid.sdk.api.MetadataValue

/**
 * Metadata value containing a double.
 *
 * @property content the double value
 */
data class DoubleMetadataValue(
    val content: Double
) : MetadataValue

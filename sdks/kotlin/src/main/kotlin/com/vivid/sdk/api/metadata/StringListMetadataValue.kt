package com.vivid.sdk.api.metadata

import com.vivid.sdk.api.MetadataValue

/**
 * Metadata value containing a list of strings.
 *
 * @property content the list of strings
 */
data class StringListMetadataValue(
    val content: List<String>
) : MetadataValue {
    fun contains(value: String): Boolean {
        return content.contains(value)
    }
}

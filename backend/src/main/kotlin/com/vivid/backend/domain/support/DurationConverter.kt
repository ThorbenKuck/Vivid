package com.vivid.backend.domain.support

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.Duration

@Converter(autoApply = true)
class DurationConverter : AttributeConverter<Duration, String> {
    override fun convertToDatabaseColumn(attribute: Duration?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): Duration? {
        return dbData?.let { Duration.parse(it) }
    }
}

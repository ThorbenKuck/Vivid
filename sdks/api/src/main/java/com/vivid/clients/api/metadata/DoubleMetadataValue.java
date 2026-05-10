package com.vivid.clients.api.metadata;

import com.vivid.clients.api.MetadataValue;

public record DoubleMetadataValue(
        Double content
) implements MetadataValue<Double> {
    @Override
    public Double getContent() {
        return content;
    }
}

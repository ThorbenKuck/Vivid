package com.vivid.clients.api.metadata;

import com.vivid.clients.api.MetadataValue;

public record BooleanMetadataValue(
        Boolean content
) implements MetadataValue<Boolean> {
    @Override
    public Boolean getContent() {
        return content;
    }
}

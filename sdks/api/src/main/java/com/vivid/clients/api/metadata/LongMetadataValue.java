package com.vivid.clients.api.metadata;

import com.vivid.clients.api.MetadataValue;

public record LongMetadataValue(Long content) implements MetadataValue<Long> {
    @Override
    public Long getContent() {
        return content;
    }
}

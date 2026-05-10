package com.vivid.clients.api.metadata;

import com.vivid.clients.api.MetadataValue;

public record StringMetadataValue(String content) implements MetadataValue<String> {
    @Override
    public String getContent() {
        return content;
    }
}

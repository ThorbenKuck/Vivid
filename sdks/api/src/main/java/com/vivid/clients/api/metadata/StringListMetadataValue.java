package com.vivid.clients.api.metadata;

import com.vivid.clients.api.MetadataValue;

import java.util.List;

public record StringListMetadataValue(List<String> content) implements MetadataValue<List<String>> {
    @Override
    public List<String> getContent() {
        return content;
    }
}

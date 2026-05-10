package com.vivid.clients.api;

import jakarta.annotation.Nullable;
import tools.jackson.databind.JsonNode;

public record UnknownMetadataValue(
        String type,
        @Nullable JsonNode content
) implements MetadataValue<JsonNode> {
    @Override
    @Nullable
    public JsonNode getContent() {
        return content;
    }
}

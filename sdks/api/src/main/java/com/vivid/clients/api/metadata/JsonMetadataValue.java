package com.vivid.clients.api.metadata;

import com.vivid.clients.api.MetadataValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record JsonMetadataValue(JsonNode content) implements MetadataValue<JsonNode> {
    @Override
    public JsonNode getContent() {
        return content;
    }

    public <T> T convertTo(ObjectMapper objectMapper, Class<T> type) {
        return objectMapper.convertValue(content, type);
    }

    public <T> T readAs(ObjectMapper objectMapper, JsonMetadataValue metadataValue, Class<T> type) {
        return metadataValue.convertTo(objectMapper, type);
    }
}

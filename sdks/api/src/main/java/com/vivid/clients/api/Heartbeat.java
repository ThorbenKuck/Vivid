package com.vivid.clients.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"applicationName", "environment", "clientToken", "streams", "clientVersion"})
public record Heartbeat(
        @Nullable String applicationName,
        String environment,
        @Nullable String clientToken,
        Set<String> streams,
        @Nullable String clientVersion
) {
}

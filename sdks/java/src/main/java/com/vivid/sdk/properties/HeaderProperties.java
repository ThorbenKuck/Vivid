package com.vivid.sdk.properties;

import jakarta.annotation.Nullable;

import java.net.http.HttpRequest;

public record HeaderProperties(
        String applicationName,
        String clientToken,
        String environment,
        Values values
) {

    public record Values(
            @Nullable String applicationName,
            @Nullable String environment,
            @Nullable String clientToken
    ) {}

    private static final String DEFAULT_APPLICATION_NAME = "X-Vivid-Application-Name";
    private static final String DEFAULT_CLIENT_TOKEN = "X-Vivid-Client-Token";
    private static final String DEFAULT_ENVIRONMENT = "X-Vivid-Environment";

    public static HeaderProperties of(Values values) {
        return new HeaderProperties(DEFAULT_APPLICATION_NAME, DEFAULT_CLIENT_TOKEN, DEFAULT_ENVIRONMENT, values);
    }

    public void applyTo(HttpRequest.Builder builder) {
        if (environment() != null) {
            builder.header(environment(), environment());
        }
        if (applicationName() != null ) {
            builder.header(applicationName(), applicationName());
        }
        if (clientToken() != null ) {
            builder.header(clientToken(), clientToken());
        }
    }

//    void applyTo(HttpHeaders headers) {
//        headers.add(applicationName, vividProperties.applicationName)
//        headers.add(environment, vividProperties.environment)
//        vividProperties.clientToken?.let { token -> headers.add(clientToken, token) }
//    }
}

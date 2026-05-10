package com.vivid.sdk.properties;

import java.net.URI;
import java.net.URISyntaxException;

public record FeatureApiProperties(
        String baseUrl,
        String environment,
        HeaderProperties headerProperties
) {

    public URI fetchSingleFeatureUri(String id) {
        try {
            return new URI(joinUrl(baseUrl, "api", "client", "features", environment, id));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to construct Feature URI", e);
        }
    }

    public URI fetchAllFeaturesUri() {
        try {
            return new URI(joinUrl(baseUrl, "api", "client", "features", environment));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to construct Feature URI", e);
        }
    }

    private static String joinUrl(String... parts) {
        if (parts.length == 0) {
            throw new IllegalArgumentException("At least one part is required");
        }
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (result.toString().endsWith("/") && part.startsWith("/")) {
                result.append(part.substring(1));
            } else if (result.toString().endsWith("/") || part.startsWith("/")) {
                result.append(part);
            } else {
                result.append("/").append(part);
            }
        }

        return result.toString();
    }
}

package com.vivid.sdk;

import com.vivid.clients.api.Feature;
import com.vivid.sdk.properties.FeatureApiProperties;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.slf4j.LoggerFactory.*;

public class HttpClientFeatureApi implements FeatureApi {

    private final HttpClient httpClient;
    private final Converter converter;
    private final FeatureApiProperties featureApiProperties;
    private final Logger logger = getLogger(HttpClientFeatureApi.class);

    public HttpClientFeatureApi(
            HttpClient httpClient,
            Converter converter,
            FeatureApiProperties featureApiProperties
    ) {
        this.httpClient = httpClient;
        this.converter = converter;
        this.featureApiProperties = featureApiProperties;
    }

    @Nullable
    @Override
    public Feature fetchFeature(String key) {
        HttpResponse<String> response = exchange(featureApiProperties.fetchSingleFeatureUri(key));

        if (Integer.toString(response.statusCode()).startsWith("2")) {
            return converter.readFeature(response.body());
        }

        logger.warn("Failed to fetch feature. {}: {}", response.statusCode(), response.body());
        return null;
    }

    @Nullable
    @Override
    public List<Feature> fetchAllFeatures() {
        HttpResponse<String> response = exchange(featureApiProperties.fetchAllFeaturesUri());

        if (Integer.toString(response.statusCode()).startsWith("2")) {
            return converter.readFeatureList(response.body());
        }

        logger.warn("Failed to fetch all features. {}: {}", response.statusCode(), response.body());
        return null;
    }

    private HttpResponse<String> exchange(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .GET();

        featureApiProperties.headerProperties().applyTo(builder);

        try {
            return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Converter {
        Feature readFeature(String json);
        List<Feature> readFeatureList(String json);
    }
}

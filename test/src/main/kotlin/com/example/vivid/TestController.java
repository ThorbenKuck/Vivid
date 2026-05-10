package com.example.vivid;

import com.vivid.clients.api.Feature;
import com.vivid.clients.api.MetadataValue;
import com.vivid.sdk.FeatureReference;
import com.vivid.sdk.Features;
import com.vivid.sdk.spring.qualifier.Vivid;
import jakarta.annotation.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class TestController {

    private final Features features;
    private final FeatureReference testfeature;

    public TestController(
            Features features,
            @Vivid("Test") FeatureReference testfeature
    ) {
        this.features = features;
        this.testfeature = testfeature;
    }

    @GetMapping("reference")
    @Nullable
    String reference() {
        return Objects.requireNonNullElse(testfeature.isEnabled(), "<null>").toString();
    }

    @GetMapping("/{feature}")
    @Nullable
    String onDemand(@PathVariable String feature) {
        return Objects.requireNonNullElse(features.get(feature).isEnabled(), "<null>").toString();
    }

    @GetMapping("/{feature}/{flag}")
    @Nullable
    String flag(@PathVariable String feature, @PathVariable String flag) {
        return Objects.requireNonNullElse(features.get(feature).isEnabled(flag), "<null>").toString();
    }

    @GetMapping("/{feature}/meta/{name}")
    @Nullable
    Object meta(@PathVariable String feature, @PathVariable String name) {
        return Objects.requireNonNullElse(features.get(feature).getMetadata(name), "<null>");
    }

    @GetMapping("/all")
    @Nullable
    List<Feature> allFeatures() {
        return features.getAll();
    }

}

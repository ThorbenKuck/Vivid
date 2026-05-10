# Vivid - Kotlin SDK

The Vivid Kotlin SDK provides the core interfaces and domain entities for interacting with the Vivid feature management platform.

## Overview

The SDK is built around a few core concepts:

- **Features**: The main entry point to query feature states.
- **FeatureOperations**: Provides methods to check if a feature is enabled and to retrieve metadata.
- **FeatureCache**: A local cache that stores feature states for fast access.
- **FeatureStream**: A source of push updates from the Vivid backend (e.g., via Kafka, SSE, or WebSockets).
- **FeatureApi**: An interface for fetching feature states from the Vivid backend (e.g., via REST).

## Core Components

### Features

The `Features` interface is the primary way to interact with the SDK.
`Features` requires a backbone cache to store feature states.
Instantiating a `Features` instance might look like this:

```kotlin
val cache = SimpleFeatureCache()
val features = Features(cache)
```

Depending on the cache implementation, the `Features` instance might behave differently.
In the above example, we need to update the cache manually.
Might be looking like this:

```kotlin
val cache = SimpleFeatureCache()
cache.set(Feature(id = "test-feature", name = "test-feature", enabled = true, flags = mapOf(), metadata = mapOf(), timestamp = Instant.now()))
val features = Features(cache)
```

Either streams or manual code could fill the cache.
Alternatively, you can use an api-aware cache that fetches missing features on demand.
This could look like this:

```kotlin
val api: FeatureApi = ...
val cache = FetchingFeatureCache(api)
val features = Features(cache)
```
Once setup, you can use `Features` to get a `FeatureOperations` instance for a specific key:

```kotlin
val features: Features = ... // Obtain instance
val ops = features.get("my-feature")

if (ops.isEnabled(defaultValue = false)) {
    // Feature is enabled
}
```

### FeatureOperations

`FeatureOperations` allows you to check the main feature flag, sub-flags, and metadata:

```kotlin
val ops = features.get("my-feature")

// Main flag
val enabled = ops.isEnabled(defaultValue = false)

// Sub-flag
val betaEnabled = ops.isEnabled("beta-access", defaultValue = false)

// Metadata
val theme = ops.getMetadata<StringMetadataValue>("theme")?.content ?: "dark"
```

### FeatureReference

A `FeatureReference` is similar to `FeatureOperations` but it always reflects the current state in the cache.
It can be useful for long-lived components.
For example, you could fetch a reference once and use it to check if a feature is enabled in your code:

```kotlin
class MyComponent(
    private val features: Featues
) {
    private val feature = features.reference("my-feature")
    
    fun doSomething() {
        // If the cache is updated, this will directly reflect the new state
        if (feature.isEnabled(defaultValue = false)) {
            // Feature is enabled
        } else {
            // Feature is disabled
        }
    }
}
```

## Implementation

The Kotlin SDK contains abstract implementations and interfaces. Concrete implementations (like the Spring Boot starter) provide the actual logic for communicating with the Vivid backend.

### Local Cache

The SDK provides a `SimpleFeatureCache` for basic in-memory storage and a `FetchingFeatureCache` that can fetch missing features on demand via a `FeatureApi`.

### Streaming Updates

To keep the local cache up to date, you can use a `FeatureStream`. When an update is received, it should be pushed into the `ModifiableFeatures` (which is usually a `CacheBasedFeatures` instance).

## Example Usage

```kotlin
// 1. Setup Cache and API
val featureApi: FeatureApi = MyRestFeatureApi()
val cache = FeatureCache(featureApi)
val features = CacheBasedFeatures(cache)

// 2. Setup Stream for updates
val stream: FeatureStream = MyKafkaFeatureStream()
stream.subscribe(object : FeatureStream.Callback {
    override fun onNext(feature: Feature) {
        features.set(feature)
    }
    override fun setAll(featureValues: List<Feature>) {
        features.setAll(featureValues)
    }
})

// 3. Use in your application
if (features.get("new-checkout-process").isEnabled(defaultValue = false)) {
    renderNewCheckout()
} else {
    renderOldCheckout()
}
```
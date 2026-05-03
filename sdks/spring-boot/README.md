# Vivid - Spring Boot Starter

The Vivid Spring Boot Starter allows for easy integration of the Vivid feature management platform into your Spring Boot application.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.vivid</groupId>
    <artifactId>spring-boot-starter-vivid</artifactId>
    <version>${vivid.version}</version>
</dependency>
```

## Configuration

You can configure the Vivid SDK in your `application.yml` or `application.properties`:

```yaml
spring:
  vivid:
    # Whether the Vivid SDK should be enabled or not (optional, default: true)
    # Useful to explicitly disable it
    enabled: true
    # The environment to fetch features for (required)
    environment: production
    # A name for your application (optional)
    # This is used to report feature usage to Vivid
    # It is recommended to set this!
    application-id: my-application
    # Controls whether streams should be automatically started on application startup (optional, default: true)
    autostart-streams: boolean
```

## Usage

### Injecting Features

You can inject the `Features` bean into your services to check feature states:

```kotlin
@Service
class MyService(
    private val features: Features,
) {

    fun doSomething() {
        if (features.get("new-feature").isEnabled(defaultValue = false)) {
            // New logic
        } else {
            // Old logic
        }
    }
}
```

This will generate a `FeatureOperation` object every time you call `features.get("new-feature)`.
If you want to avoid this, you can use the `FeatureReference`:

```kotlin
@Service
class MyService(
    features: Features
) {

    val newFeature = features.reference("new-feature")

    fun doSomething() {
        if (newFeature.isEnabled(defaultValue = false)) {
            // New logic
        } else {
            // Old logic
        }
    }
}
```

This way you can avoid the overhead of creating a `FeatureOperation` object every time you are evaluating a feature.
A short hand for this is to use an injection qualifier:

```kotlin
@Service
class MyService(
    /**
     * A reference to the 'new-feature' feature.
     * 
     * Will be updated automatically when the feature state changes.
     */
    @Vivid("new-feature")
    val newFeatureReference: FeatureReference,
    /**
     * An operation to the 'new-feature' feature.
     * 
     * Will not change automatically and will therefore be static for the lifetime of the component
     */
    @Vivid("new-feature")
    val newFeatureOperation: FeatureOperation,
) {
    fun doSomething() {
        // Use the reference or operation as you like
    }
}
```

### Conditional Beans

The starter provides conditional annotations to enable or disable beans based on feature flag states:

```kotlin
@Configuration
class MyConfiguration {

    @Bean
    @ConditionalOnVivid("rest") // Only if 'rest' stream is enabled
    fun restRelatedBean() = ...

    @Bean
    @ConditionalOnVivid // Only if Vivid is enabled
    fun vividRelatedBean() = ...
}
```

## Supported Streams

- **REST**: Polls the Vivid backend at regular intervals. Requires `spring-boot-starter-web`.
- **Kafka**: Receives updates via a Kafka topic. Requires `spring-kafka`.
- **SSE**: Receives updates via Server-Sent Events. Requires `spring-boot-starter-webflux`.

### Configurations

#### REST

```yaml
spring.vivid.rest:
  # Whether the REST stream should be enabled or not (optional, default: true)
  enabled: boolean
  # The url of the Vivid backend (required)
  # If absent, the SDK will not start up the REST stream
  base-url: https://vivid.example.com
  # An optional API token to authenticate with the Vivid backend (optional)
  api-token: my-secret-token
  # The header name this sdk will use to report the application id (optional, default: X-Application-Id)
  # It is recommended to not change this, only if you have a proxy that requires it
  application-id-header-name: X-Application-Id
  # The header name this sdk will use to report the api token (optional, default: X-Application-Id)
  # It is recommended to not change this, only if you have a proxy that requires it
  api-token-header-name: X-Api-Token
```

#### SSE

The SSE properties are identical to the REST ones.
By default, they are taken from the REST properties.
As SSE may require additional infrastructure, they can be overwritten explicitly.

```yaml
spring.vivid.rest:
  # Whether the REST stream should be enabled or not (optional, default: true)
  enabled: boolean
  # The url of the Vivid backend (required, default: ${spring.vivid.rest.base-url})
  base-url: https://vivid.example.com
  # An optional API token to authenticate with the Vivid backend (optional, default: ${spring.vivid.rest.api-token})
  api-token: my-secret-token
  # The header name this sdk will use to report the application id (optional, default: ${spring.vivid.rest.application-id-header-name})
  application-id-header-name: X-Application-Id
  # The header name this sdk will use to report the api token (optional, default: ${spring.vivid.rest.api-token-header-name})
  api-token-header-name: X-Api-Token
```

#### Kafka

```yaml
spring.vivid.kafka:
  # Whether the Kafka stream should be enabled or not (optional, default: true)
  enabled: true
  # The bootstrap servers that Vivid should connect to (optional, default: ${spring.kafka.bootstrap-servers})
  bootstrap-servers: localhost:9092
  # The consumer group id (optional, default: ${spring.kafka.consumer.group-id})
  group-id: my-application
  # The topics to subscribe to (required)
  topics:
    - vivid-features
  # Normally spring contains one consumer-factory bean in the application context.
  # This single instance is modified to support multiple different consumers.
  # Optionally, you can specify a different consumer-factory beans to use in your application.
  # This section allows you to specify how vivid should get the consumer-factory bean.
  consumer-factory:
    # How Vivid should get the consumer-factory bean.
    # Options:
    # - vivid: Vivid will construct a standalone consumer-factory bean.
    # - application-context: Vivid will use the consumer-factory bean from the application context.
    # - bean-name: Vivid will use the consumer-factory bean with the specified name. Requires the additional property bean-name.
    # By default, vivid will construct a standalone consumer-factory.
    # It is recommended to leave it this way.
    take-from: vivid # vivid, application-context or bean-name.
```

## Internal Architecture

The starter automatically configures:
1. A `FeatureCache` (defaults to `FetchingFeatureCache` if a `FeatureApi` is present).
2. A `FeatureApi` (defaults to `SpringFeatureApi` for REST).
3. `FeatureStream` implementations based on the enabled streams.
4. A `FeatureStreamAggregator` that manages the streams.

## DB Cache

To use the `JdbcFeatureCache`, you need to setup the following tables in your DB:

```sql
CREATE TABLE features (
    id VARCHAR(255) PRIMARY KEY,
    payload TEXT NOT NULL,
    version_timestamp TIMESTAMP NOT NULL
);

CREATE TABLE feature_aliases (
    alias_name VARCHAR(255) PRIMARY KEY,
    feature_id VARCHAR(255) NOT NULL,
    CONSTRAINT fk_feature FOREIGN KEY (feature_id) REFERENCES features(id) ON DELETE CASCADE
);
```

# SDKs

Integrating Vivid with your applications is simple. We provide high-performance SDKs for popular languages and frameworks.

## Kotlin SDK

The core Kotlin SDK is lightweight and designed for use in any JVM-based application.

### Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.vivid</groupId>
  <artifactId>vivid-sdk-kotlin</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Basic Usage

Initialize the `VividClient` and start checking features:

```kotlin
val client = VividClient.builder()
    .baseUrl("http://localhost:8080")
    .environment("Production")
    .build()

if (client.isEnabled("new-feature-flag")) {
    // Execute new feature logic
} else {
    // Fallback to existing logic
}
```

## Spring Boot Starter

For Spring Boot applications, we provide a starter that automates configuration and provides easy-to-use annotations.

### Installation

Add the starter to your `pom.xml`:

```xml
<dependency>
  <groupId>com.vivid</groupId>
  <artifactId>vivid-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Configuration

Configure your environment in `application.yml`:

```yaml
vivid:
  base-url: http://localhost:8080
  environment: Production
```

### Advanced Usage with Metadata

Access metadata dynamically within your application:

```kotlin
@Service
class FeatureService(private val vividClient: VividClient) {
    fun process() {
        val metadata = vividClient.getMetadata("api-config")
        val timeout = metadata["timeout"]?.toLong() ?: 5000L
        // Use the dynamic timeout value
    }
}
```

:::info
Vivid SDKs are designed for efficiency, with built-in caching and optimized polling for flag updates.
:::

## Other Languages

Currently, we prioritize Kotlin and Java-based ecosystems. If you need SDKs for other languages (e.g., Python, Go, Node.js), you can directly interact with our [Client API](/docs/introduction#high-level-architecture).

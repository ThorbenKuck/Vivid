# Backend Metadata Refactoring and Angular Frontend Generation

Juniper, we will now start with creating the angular frontend. But before generating the Angular frontend, you must refactor the backend to strongly type the metadata structure. The current implementation using Map<String, Any?> is not acceptable because it produces weak typing in the generated OpenAPI specification and therefore weak DTOs in the frontend and SDKs. The metadata model must be redesigned using Jackson polymorphic inheritance so that metadata values are explicitly typed and safely serializable.

The metadata structure must use a sealed class hierarchy in Kotlin. The base class must be named MetadataValue. It must use Jackson polymorphic type handling with @JsonTypeInfo and @JsonSubTypes. The discriminator property must be named @type. The JSON structure must look like this:

```json
{
"@type": "Boolean",
"content": true
}
```

The following concrete implementations must exist.

- BooleanMetadataValue with a Boolean content field.
- LongMetadataValue with a Long content field.
- DoubleMetadataValue with a Double content field.
- StringMetadataValue with a String content field.
- JsonMetadataValue with a JsonNode content field.
- StringListMetadataValue with a List<String> content field.

The Feature aggregate must no longer store metadata as Map<String, Any?>. Instead, it must use Map<String, MetadataValue>.
Persistence must correctly serialize and deserialize the metadata JSON column using Jackson. The OpenAPI specification must correctly expose the polymorphic schema so that the generated Angular DTOs contain discriminated union types instead of weakly typed objects.

After this backend refactoring is complete and builds successfully, generate the Angular frontend project.

The frontend must be a mobile first Angular application. It must consume the backend OpenAPI specification and generate DTOs automatically using an OpenAPI generator during build time. The generated DTOs must reflect the strongly typed metadata inheritance structure.

The frontend layout must follow this structure. A header row at the top. Below the header, a two column layout. On the left side a collapsible navigation sidebar. On the right side the main content area. Layout must be implemented primarily using CSS Flexbox without heavy layout frameworks.

The design must use a soft green based color palette. The design must support light and dark themes. Dark theme must be the default. Theme switching must be implemented in a simple and maintainable way, for example using CSS variables and a theme service.

The Angular project must separate HTML, TypeScript and CSS files for every component. No inline templates and no inline styles.

All environment dependent values such as backend base URL must be configurable via Angular environment files.

The frontend must communicate directly with the backend using the generated DTOs and typed API services. There must be no manual duplication of API models.

Focus on clean structure and long term maintainability. The goal is to create a production ready foundation that cleanly integrates backend, OpenAPI generation, and Angular consumption with strong typing across the entire stack.
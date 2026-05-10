# Client API

The client API is a JDK-based api definition for the endpoints exposed by Vivid.
It contains the classes returned by the endpoints and can be used to build a client.

To use it, add the following dependency to your project:

```xml
<dependency>
    <groupId>com.vivid</groupId>
    <artifactId>client-api</artifactId>
    <version>${vivid.version}</version>
</dependency>
```

This dependency brings the primary classes `Feature` and `Heartbeat`.
Normally you'd not add this dependency directly to your client.
Instead, you add one of the SDKs to your project.
These SKDs bundle the api and provide an implementation which uses the API classes.
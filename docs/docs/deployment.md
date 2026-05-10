import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Deploying Vivid

Vivid is separated into two major parts, the frontend and the backend.
Both need to be deployed separately.

## Backend Deployment

The backend is a standalone JAR file.
Download the latest JAR file from the [releases page](https://github.com/ThorbenKuck/vivid/releases).
Then run the JAR file using:

```bash
java -jar vivid-backend.jar
```

:::note

Vivid is built on top of spring-boot.
This means you can provide all the known arguments to the JAR file.
You can even provide custom agents like OTEL to integrate Vivid into your ecosystem.

:::

To configure Vivid, create an application properties file understandable by spring-boot (like `application.properties` or `application.yaml`) in the same directory as the JAR file.
In our example we'll use `application.yaml`.

Vivid requires a database to store the data, more precisely a PostgreSQL database.
Assuming you have setup a PostgreSQL database, you can provide credentials either as environment variables or in the application properties file.

<Tabs>
  <TabItem value="properties" label="application.yaml" default>
```yaml
application:
  database:
    url: jdbc:postgresql://localhost:5432/vivid
    username: super-secure-user
    password: super-secure-password
```
  </TabItem>
  <TabItem value="env" label="Environment Variables">
```text
DATABASE_URL=jdbc:postgresql://localhost:5432/vivid
DATABASE_USERNAME=super-secure-user
DATABASE_PASSWORD=super-secure-password
```
  </TabItem>
</Tabs>

With this configuration, Vivid will start up and connect to the database you provided.
Under the hood, Vivid uses [Flyway](https://flywaydb.org/) to manage the database schema on startup.
So, nothing else to do on this front.

### Actuator

Vivid comes with the spring-boot actuator module on board.

**Configuration**
- Port: `8081`
- Exposed endpoints: `all`
- Path: `/actuator`

Every endpoint is prefixed with `/actuator` and each actuator module is exposed.
For example, you can access the health endpoint by going to [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health).
To see all available endpoints, visit [http://localhost:8081/actuator](http://localhost:8081/actuator).

### Metrics

To monitor Vivid you can use the metrics endpoint by the actuator ([http://localhost:8081/actuator/metrics](http://localhost:8081/actuator/metrics)).    
Additionally, Vivid comes preconfigured with a prometheus endpoint ([http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus)).

### Security Configuration

If you started Vivid without any security configuration, you can use Vivid without any problem.
But that also means anyone can access and do everything with your data.

But fear not brave adventurer!
Vivid supports OIDC authentication.

All you need to configure Vivid to use OIDC ar the following variables:

<Tabs>
  <TabItem value="properties" label="application.yaml" default>
```yaml
application:
  oidc:
    issuer-url: http://localhost:8989/realms/vivid
    issuer-name: Local KeyCloak
    client-id: vivid-client
    frontend-base-url: http://localhost:4200
```
  </TabItem>
  <TabItem value="env" label="Environment Variables">
```text
OIDC_ISSUER_URL=http://localhost:8989/realms/vivid
OIDC_ISSUER_NAME=Local KeyCloak
OIDC_CLIENT_ID=vivid-client
OIDC_FRONTEND_BASE_URL=http://localhost:4200
```
  </TabItem>
</Tabs>

- `issuer-url` is the URL of your OIDC provider.
- `issuer-name` is the name of your OIDC provider. This is optional and used in the frontend to display the name of your OIDC provider.
- `client-id` is the client id of your OIDC client.
- `frontend-base-url` is the base URL of your frontend application. Used for redirecting the user back to Vivid after authentication.

In the above example, we have setup a local KeyCloak instance on port 8989, the frontend is running on port 4200, and the backend is running on port 8080.

Once you have configured Vivid to use OIDC, you can grant your users access to Vivid and controlling access to by assigning roles to them.
See the [security documentation](security/permissions) for more details.

## Frontend Deployment

To deploy the frontend, you need to download the latest release from the [releases page](https://github.com/ThorbenKuck/vivid/releases).
Then extract the archive and run the `npm run start` command.

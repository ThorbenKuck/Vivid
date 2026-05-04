DROP TABLE IF EXISTS feature_usages;
DROP TABLE IF EXISTS vivid_client_technologies;
DROP TABLE IF EXISTS vivid_client_presence_technologies;
DROP TABLE IF EXISTS vivid_client_presences;
DROP TABLE IF EXISTS vivid_clients;

CREATE TABLE vivid_clients
(
    id           UUID PRIMARY KEY,
    client_name  VARCHAR(255) NOT NULL UNIQUE,
    client_token VARCHAR(255)
);

CREATE TABLE vivid_client_presences
(
    id             UUID PRIMARY KEY,
    client_id      UUID      NOT NULL REFERENCES vivid_clients (id) ON DELETE CASCADE,
    environment_id UUID      NOT NULL REFERENCES environments (id) ON DELETE CASCADE,
    last_seen      TIMESTAMP NOT NULL,
    client_version VARCHAR(255),
    UNIQUE (client_id, environment_id)
);

CREATE TABLE vivid_client_presence_technologies
(
    presence_id UUID         NOT NULL REFERENCES vivid_client_presences (id) ON DELETE CASCADE,
    technology  VARCHAR(255) NOT NULL,
    PRIMARY KEY (presence_id, technology)
);

CREATE TABLE feature_usages
(
    feature_id     UUID      NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    client_id      UUID      NOT NULL REFERENCES vivid_clients (id) ON DELETE CASCADE,
    environment_id UUID      NOT NULL REFERENCES environments (id) ON DELETE CASCADE,
    last_seen      TIMESTAMP NOT NULL,
    PRIMARY KEY (feature_id, client_id, environment_id)
);

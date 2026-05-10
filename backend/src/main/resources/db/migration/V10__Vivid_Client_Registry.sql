CREATE TABLE IF NOT EXISTS vivid_clients
(
    id             UUID PRIMARY KEY,
    client_token   VARCHAR(255),
    client_name    VARCHAR(255) NOT NULL,
    environment_id UUID         NOT NULL REFERENCES environments (id) ON DELETE CASCADE,
    last_seen      TIMESTAMP,
    client_version VARCHAR(255),
    UNIQUE (client_name, environment_id)
);

CREATE TABLE vivid_client_technologies
(
    client_id  UUID         NOT NULL REFERENCES vivid_clients (id) ON DELETE CASCADE,
    technology VARCHAR(255) NOT NULL,
    PRIMARY KEY (client_id, technology)
);

CREATE TABLE IF NOT EXISTS vivid_settings
(
    id                                UUID PRIMARY KEY,
    active                            BOOLEAN   NOT NULL DEFAULT FALSE,
    last_updated                      TIMESTAMP NOT NULL,
    allow_dynamic_client_registration BOOLEAN   NOT NULL DEFAULT TRUE,
    require_client_tokens             BOOLEAN   NOT NULL DEFAULT TRUE,
    online_threshold                  TEXT      NOT NULL DEFAULT 'PT5M'
);
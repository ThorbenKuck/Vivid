CREATE TABLE feature_usages
(
    feature_id UUID      NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    client_id  UUID      NOT NULL REFERENCES vivid_clients (id) ON DELETE CASCADE,
    last_seen  TIMESTAMP NOT NULL,
    PRIMARY KEY (feature_id, client_id)
);

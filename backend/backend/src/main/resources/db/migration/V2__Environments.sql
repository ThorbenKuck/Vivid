CREATE TABLE environments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE feature_environments (
    id UUID PRIMARY KEY,
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    environment_id UUID NOT NULL REFERENCES environments(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB,
    CONSTRAINT uq_feature_environment UNIQUE (feature_id, environment_id)
);

CREATE INDEX idx_feature_environments_feature ON feature_environments(feature_id);
CREATE INDEX idx_feature_environments_environment ON feature_environments(environment_id);
CREATE INDEX idx_feature_environments_enabled ON feature_environments(enabled);

CREATE TABLE feature_environment_flags (
    feature_environment_id UUID NOT NULL REFERENCES feature_environments(id) ON DELETE CASCADE,
    flag_key VARCHAR(255) NOT NULL,
    flag_value BOOLEAN NOT NULL,
    PRIMARY KEY (feature_environment_id, flag_key)
);

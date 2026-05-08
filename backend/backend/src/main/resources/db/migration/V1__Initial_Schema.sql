CREATE TABLE features (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB
);

CREATE INDEX idx_features_name ON features(name);
CREATE INDEX idx_features_enabled ON features(enabled);

CREATE TABLE feature_flags (
    feature_id UUID NOT NULL REFERENCES features(id),
    flag_key VARCHAR(255) NOT NULL,
    flag_value BOOLEAN NOT NULL,
    PRIMARY KEY (feature_id, flag_key)
);

CREATE TABLE feature_tags (
    feature_id UUID NOT NULL REFERENCES features(id),
    tag VARCHAR(255) NOT NULL
);

CREATE TABLE feature_links (
    id UUID PRIMARY KEY,
    source_feature_id UUID NOT NULL REFERENCES features(id),
    target_feature_id UUID NOT NULL REFERENCES features(id),
    link_type VARCHAR(255)
);

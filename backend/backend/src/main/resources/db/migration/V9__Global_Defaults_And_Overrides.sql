-- V9: Refactor to Global Defaults with Environment Overrides

-- Create new tables for overrides
CREATE TABLE environment_overrides (
    id UUID PRIMARY KEY,
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    environment_id UUID NOT NULL REFERENCES environments(id) ON DELETE CASCADE,
    enabled BOOLEAN,
    metadata JSONB,
    strategy VARCHAR(50) NOT NULL DEFAULT 'OVERRIDE',
    CONSTRAINT uq_environment_override UNIQUE (feature_id, environment_id)
);

CREATE TABLE environment_override_flags (
    override_id UUID NOT NULL REFERENCES environment_overrides(id) ON DELETE CASCADE,
    flag_key VARCHAR(255) NOT NULL,
    flag_value BOOLEAN NOT NULL,
    PRIMARY KEY (override_id, flag_key)
);

-- Drop old environment tables
DROP TABLE IF EXISTS feature_environment_flags;
DROP TABLE IF EXISTS feature_environments;

-- Ensure features table has correct columns (they should be there from V1, but let's be safe if they were dropped)
-- Note: V1 already has enabled and metadata.

-- Add flags table if missing (V1 had it)
-- CREATE TABLE IF NOT EXISTS feature_flags (
--    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
--    flag_key VARCHAR(255) NOT NULL,
--    flag_value BOOLEAN NOT NULL,
--    PRIMARY KEY (feature_id, flag_key)
-- );

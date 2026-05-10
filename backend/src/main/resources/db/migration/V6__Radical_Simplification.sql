-- Drop Team related tables
DROP TABLE IF EXISTS feature_teams;
DROP TABLE IF EXISTS team_members;
DROP TABLE IF EXISTS teams CASCADE;

-- Remove foreign key constraints and columns for departments
ALTER TABLE features DROP COLUMN IF EXISTS department_id;
ALTER TABLE environments DROP COLUMN IF EXISTS department_id;
ALTER TABLE vivid_users DROP COLUMN IF EXISTS department_id;

-- Drop departments table
DROP TABLE IF EXISTS departments CASCADE;

-- Create notes table
CREATE TABLE notes (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    author_id UUID NOT NULL REFERENCES vivid_users(id) ON DELETE CASCADE,
    timestamp TIMESTAMPTZ NOT NULL,
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE
);

CREATE INDEX idx_notes_feature_id ON notes(feature_id);
CREATE INDEX idx_notes_timestamp ON notes(timestamp DESC);

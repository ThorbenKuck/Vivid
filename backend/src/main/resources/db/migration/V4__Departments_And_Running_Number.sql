CREATE TABLE departments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

INSERT INTO departments (id, name, description) VALUES ('00000000-0000-0000-0000-000000000000', 'General', 'Default department');

ALTER TABLE features ADD COLUMN department_id UUID REFERENCES departments(id);
ALTER TABLE teams ADD COLUMN department_id UUID REFERENCES departments(id);
ALTER TABLE environments ADD COLUMN department_id UUID REFERENCES departments(id);
ALTER TABLE vivid_users ADD COLUMN department_id UUID REFERENCES departments(id);

UPDATE features SET department_id = '00000000-0000-0000-0000-000000000000';
UPDATE teams SET department_id = '00000000-0000-0000-0000-000000000000';
UPDATE environments SET department_id = '00000000-0000-0000-0000-000000000000';
UPDATE vivid_users SET department_id = '00000000-0000-0000-0000-000000000000';

ALTER TABLE features ALTER COLUMN department_id SET NOT NULL;
ALTER TABLE teams ALTER COLUMN department_id SET NOT NULL;
ALTER TABLE environments ALTER COLUMN department_id SET NOT NULL;
ALTER TABLE vivid_users ALTER COLUMN department_id SET NOT NULL;

-- Running number for features
DROP SEQUENCE IF EXISTS feature_running_number_seq;
CREATE SEQUENCE feature_running_number_seq START 1;
ALTER TABLE features ADD COLUMN running_number BIGINT;

-- Assign running numbers to existing features
UPDATE features SET running_number = nextval('feature_running_number_seq');

ALTER TABLE features ALTER COLUMN running_number SET NOT NULL;
ALTER TABLE features ADD CONSTRAINT uq_feature_running_number UNIQUE (running_number);

-- Add key column
alter table features add column key text;

-- Populate key column with name values
update features set key = replace(lower(trim(name)), ' ', '-') where key is null;

-- Ensure data integrity
alter table features alter column key set not null;
alter table features add constraint features_key_unique unique (key);
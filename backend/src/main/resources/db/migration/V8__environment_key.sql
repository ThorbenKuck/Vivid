-- Add key column
alter table environments add column key text;
alter table environments add column weight integer;

-- Populate key column with name values
update environments set key = replace(lower(trim(name)), ' ', '-') where key is null;

-- Ensure data integrity
alter table environments alter column key set not null;
alter table environments add constraint environments_key_unique unique (key);
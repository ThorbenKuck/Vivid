UPDATE environments SET weight = 0 WHERE weight IS NULL;
ALTER TABLE environments RENAME COLUMN weight TO sort_order;
ALTER TABLE environments ALTER COLUMN sort_order SET NOT NULL;
ALTER TABLE environments ALTER COLUMN sort_order SET DEFAULT 0;
ALTER TABLE environments ADD COLUMN rules JSONB NOT NULL DEFAULT '[]'::jsonb;

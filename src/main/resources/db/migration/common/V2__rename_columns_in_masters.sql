-- Rename columns
ALTER TABLE masters RENAME COLUMN lastrecharge TO last_recharge;
ALTER TABLE masters RENAME COLUMN parenttype TO parent_type;

-- Drop old index (if it existed)
DROP INDEX IF EXISTS idx_masters_last_recharge;

-- Create correct index on renamed column
CREATE INDEX IF NOT EXISTS idx_masters_last_recharge ON masters(last_recharge);

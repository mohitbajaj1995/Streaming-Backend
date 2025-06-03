ALTER TABLE refunds RENAME COLUMN parentid TO parent_id;
ALTER TABLE refunds RENAME COLUMN userid TO user_id;
ALTER TABLE refunds RENAME COLUMN subscriptionname TO subscription_name;
ALTER TABLE refunds RENAME COLUMN refundingmonths TO refunding_months;
ALTER TABLE refunds RENAME COLUMN subscriptionstartedat TO subscription_started_at;

-- Drop old index (if it existed)
DROP INDEX IF EXISTS idx_refunds_user_id;
DROP INDEX IF EXISTS idx_refunds_parent_id;

-- Create correct index on renamed column
CREATE INDEX IF NOT EXISTS idx_refunds_user_id ON refunds(user_id);
CREATE INDEX IF NOT EXISTS idx_refunds_parent_id ON refunds(parent_id);

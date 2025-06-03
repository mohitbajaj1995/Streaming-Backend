-- Rename columns
ALTER TABLE subscribers RENAME COLUMN lastrecharge TO last_recharge;
ALTER TABLE subscribers RENAME COLUMN parenttype TO parent_type;
ALTER TABLE subscribers RENAME COLUMN refundablemonths TO refundable_months;
ALTER TABLE subscribers RENAME COLUMN canrefund TO can_refund;
ALTER TABLE subscribers RENAME COLUMN startat TO start_at;
ALTER TABLE subscribers RENAME COLUMN endat TO end_at;




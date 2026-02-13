-- Remove unused option fields from product_variants table
ALTER TABLE product_variants
    DROP COLUMN IF EXISTS option1,
    DROP COLUMN IF EXISTS option2,
    DROP COLUMN IF EXISTS option3;

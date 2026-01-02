-- Migration script to change image_url columns from VARCHAR(500) to TEXT
-- Run this script to fix the "value too long" error for base64 images

-- Menu Categories
ALTER TABLE menu_categories 
ALTER COLUMN image_url TYPE TEXT;

-- Menu Items
ALTER TABLE menu_items 
ALTER COLUMN image_url TYPE TEXT;

-- Combos
ALTER TABLE combos 
ALTER COLUMN image_url TYPE TEXT;

COMMENT ON COLUMN menu_categories.image_url IS 'Base64 encoded image or image URL';
COMMENT ON COLUMN menu_items.image_url IS 'Base64 encoded image or image URL';
COMMENT ON COLUMN combos.image_url IS 'Base64 encoded image or image URL';


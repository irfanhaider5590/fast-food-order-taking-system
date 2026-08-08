-- Generic multi-shop foundation + naming/index fixes
-- Safe for existing single-shop databases (baseline-on-migrate + additive)

CREATE TABLE IF NOT EXISTS shops (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO shops (id, code, name, is_active)
SELECT 1, 'DEFAULT', 'Default Shop', true
WHERE NOT EXISTS (SELECT 1 FROM shops WHERE id = 1);

SELECT setval(pg_get_serial_sequence('shops', 'id'), GREATEST((SELECT MAX(id) FROM shops), 1));

-- Rename singular license table to plural
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'license')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'licenses') THEN
        ALTER TABLE license RENAME TO licenses;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS licenses (
    id BIGSERIAL PRIMARY KEY,
    license_key VARCHAR(255) NOT NULL UNIQUE,
    license_type VARCHAR(20) NOT NULL,
    duration_days INTEGER NOT NULL,
    activated_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    machine_id VARCHAR(255),
    client_name VARCHAR(255),
    client_email VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add shop_id columns (nullable first, then backfill, then NOT NULL where applicable)
ALTER TABLE branches ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE menu_categories ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE add_ons ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE combos ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE stock_items ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE settings ADD COLUMN IF NOT EXISTS shop_id BIGINT;
ALTER TABLE licenses ADD COLUMN IF NOT EXISTS shop_id BIGINT;

UPDATE branches SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE users SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE menu_categories SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE menu_items SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE add_ons SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE combos SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE vouchers SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE orders SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE stock_items SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE settings SET shop_id = 1 WHERE shop_id IS NULL;
UPDATE licenses SET shop_id = 1 WHERE shop_id IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_branches_shop') THEN
        ALTER TABLE branches ADD CONSTRAINT fk_branches_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_shop') THEN
        ALTER TABLE users ADD CONSTRAINT fk_users_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_menu_categories_shop') THEN
        ALTER TABLE menu_categories ADD CONSTRAINT fk_menu_categories_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_menu_items_shop') THEN
        ALTER TABLE menu_items ADD CONSTRAINT fk_menu_items_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_add_ons_shop') THEN
        ALTER TABLE add_ons ADD CONSTRAINT fk_add_ons_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_combos_shop') THEN
        ALTER TABLE combos ADD CONSTRAINT fk_combos_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_vouchers_shop') THEN
        ALTER TABLE vouchers ADD CONSTRAINT fk_vouchers_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_orders_shop') THEN
        ALTER TABLE orders ADD CONSTRAINT fk_orders_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stock_items_shop') THEN
        ALTER TABLE stock_items ADD CONSTRAINT fk_stock_items_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_settings_shop') THEN
        ALTER TABLE settings ADD CONSTRAINT fk_settings_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_licenses_shop') THEN
        ALTER TABLE licenses ADD CONSTRAINT fk_licenses_shop FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
END $$;

ALTER TABLE branches ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE users ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE menu_categories ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE menu_items ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE add_ons ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE combos ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE vouchers ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE orders ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE stock_items ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE settings ALTER COLUMN shop_id SET DEFAULT 1;
ALTER TABLE licenses ALTER COLUMN shop_id SET DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_branches_shop_id ON branches(shop_id);
CREATE INDEX IF NOT EXISTS idx_users_shop_id ON users(shop_id);
CREATE INDEX IF NOT EXISTS idx_menu_categories_shop_id ON menu_categories(shop_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_shop_id ON menu_items(shop_id);
CREATE INDEX IF NOT EXISTS idx_add_ons_shop_id ON add_ons(shop_id);
CREATE INDEX IF NOT EXISTS idx_combos_shop_id ON combos(shop_id);
CREATE INDEX IF NOT EXISTS idx_vouchers_shop_id ON vouchers(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_shop_id ON orders(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_shop_status ON orders(shop_id, order_status);
CREATE INDEX IF NOT EXISTS idx_orders_customer_phone ON orders(customer_phone);
CREATE INDEX IF NOT EXISTS idx_stock_items_shop_id ON stock_items(shop_id);
CREATE INDEX IF NOT EXISTS idx_settings_shop_id ON settings(shop_id);
CREATE INDEX IF NOT EXISTS idx_licenses_shop_id ON licenses(shop_id);
CREATE INDEX IF NOT EXISTS idx_licenses_machine_active ON licenses(machine_id, is_active);

-- Ensure order_item_add_ons exists when base order tables are already present
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'order_items')
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'add_ons')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'order_item_add_ons') THEN
        CREATE TABLE order_item_add_ons (
            id BIGSERIAL PRIMARY KEY,
            order_item_id BIGINT NOT NULL,
            add_on_id BIGINT NOT NULL,
            add_on_name_en VARCHAR(255) NOT NULL,
            add_on_name_ur VARCHAR(255),
            price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
            quantity INTEGER NOT NULL DEFAULT 1,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_order_item_add_ons_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
            CONSTRAINT fk_order_item_add_ons_addon FOREIGN KEY (add_on_id) REFERENCES add_ons(id)
        );
        CREATE INDEX idx_order_item_add_ons_order_item ON order_item_add_ons(order_item_id);
        CREATE INDEX idx_order_item_add_ons_addon ON order_item_add_ons(add_on_id);
    END IF;
END $$;

-- Stock Management Schema
-- Run this script to add stock management functionality

-- Stock Items table
CREATE TABLE IF NOT EXISTS stock_items (
    id BIGSERIAL PRIMARY KEY,
    name_en VARCHAR(255) NOT NULL,
    name_ur VARCHAR(255),
    description_en TEXT,
    description_ur TEXT,
    unit VARCHAR(50) NOT NULL DEFAULT 'piece',
    current_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    min_threshold DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_stock_items_active ON stock_items(is_active);
CREATE INDEX IF NOT EXISTS idx_stock_items_threshold ON stock_items(current_quantity, min_threshold);

COMMENT ON TABLE stock_items IS 'Stores stock items like dough, cheese, chicken, etc.';

-- Menu Item Ingredients table (links menu items to stock items)
CREATE TABLE IF NOT EXISTS menu_item_ingredients (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    stock_item_id BIGINT NOT NULL,
    quantity_required DECIMAL(10, 2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_item_ingredients_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_item_ingredients_stock_item FOREIGN KEY (stock_item_id) REFERENCES stock_items(id) ON DELETE CASCADE,
    CONSTRAINT uk_menu_item_ingredient UNIQUE (menu_item_id, stock_item_id)
);

CREATE INDEX IF NOT EXISTS idx_menu_item_ingredients_menu_item ON menu_item_ingredients(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_ingredients_stock_item ON menu_item_ingredients(stock_item_id);

COMMENT ON TABLE menu_item_ingredients IS 'Defines which stock items and quantities are needed for each menu item';

-- Stock Transactions table (tracks all stock movements)
CREATE TABLE IF NOT EXISTS stock_transactions (
    id BIGSERIAL PRIMARY KEY,
    stock_item_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'PURCHASE', 'SALE', 'ADJUSTMENT', 'WASTAGE'
    quantity DECIMAL(10, 2) NOT NULL,
    previous_quantity DECIMAL(10, 2) NOT NULL,
    new_quantity DECIMAL(10, 2) NOT NULL,
    reference_type VARCHAR(50), -- 'ORDER', 'MANUAL', 'PURCHASE', etc.
    reference_id BIGINT, -- Order ID or other reference
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT fk_stock_transactions_stock_item FOREIGN KEY (stock_item_id) REFERENCES stock_items(id)
);

CREATE INDEX IF NOT EXISTS idx_stock_transactions_stock_item ON stock_transactions(stock_item_id);
CREATE INDEX IF NOT EXISTS idx_stock_transactions_type ON stock_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_stock_transactions_reference ON stock_transactions(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_stock_transactions_date ON stock_transactions(created_at);

COMMENT ON TABLE stock_transactions IS 'Tracks all stock movements for audit and history';

-- Stock Warnings table (tracks low stock warnings)
CREATE TABLE IF NOT EXISTS stock_warnings (
    id BIGSERIAL PRIMARY KEY,
    stock_item_id BIGINT NOT NULL,
    warning_message_en TEXT NOT NULL,
    warning_message_ur TEXT NOT NULL,
    current_quantity DECIMAL(10, 2) NOT NULL,
    threshold_quantity DECIMAL(10, 2) NOT NULL,
    is_acknowledged BOOLEAN NOT NULL DEFAULT false,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_warnings_stock_item FOREIGN KEY (stock_item_id) REFERENCES stock_items(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stock_warnings_stock_item ON stock_warnings(stock_item_id);
CREATE INDEX IF NOT EXISTS idx_stock_warnings_acknowledged ON stock_warnings(is_acknowledged);
CREATE INDEX IF NOT EXISTS idx_stock_warnings_created_at ON stock_warnings(created_at);

COMMENT ON TABLE stock_warnings IS 'Stores low stock warnings that are shown every 2 hours';


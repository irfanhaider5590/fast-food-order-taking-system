-- Stock consumption config: 1 stock unit covers N servings of a menu product (optional size)

CREATE TABLE IF NOT EXISTS stock_item_consumptions (
    id                  BIGSERIAL PRIMARY KEY,
    stock_item_id       BIGINT       NOT NULL,
    menu_item_id        BIGINT       NOT NULL,
    size_code           VARCHAR(10),
    servings_per_unit   DECIMAL(12, 4) NOT NULL,
    quantity_per_serving DECIMAL(12, 6) NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sic_stock_item FOREIGN KEY (stock_item_id) REFERENCES stock_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_sic_menu_item  FOREIGN KEY (menu_item_id)  REFERENCES menu_items(id)  ON DELETE CASCADE,
    CONSTRAINT chk_sic_servings_positive CHECK (servings_per_unit > 0),
    CONSTRAINT chk_sic_qty_positive CHECK (quantity_per_serving > 0)
);

-- Unique per stock + menu + size (NULL size treated as '')
CREATE UNIQUE INDEX IF NOT EXISTS uk_stock_item_consumption
    ON stock_item_consumptions (stock_item_id, menu_item_id, COALESCE(size_code, ''));

CREATE INDEX IF NOT EXISTS idx_sic_stock_item ON stock_item_consumptions(stock_item_id);
CREATE INDEX IF NOT EXISTS idx_sic_menu_item ON stock_item_consumptions(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_sic_menu_size ON stock_item_consumptions(menu_item_id, size_code);

COMMENT ON TABLE stock_item_consumptions IS
    'Yield config: 1 unit of stock_item covers servings_per_unit of menu_item (optional size). quantity_per_serving = 1/servings used at sale time.';

-- Migrate legacy menu_item_ingredients → stock-centric yield rows (size_code NULL = any/no size)
INSERT INTO stock_item_consumptions (
    stock_item_id,
    menu_item_id,
    size_code,
    servings_per_unit,
    quantity_per_serving,
    created_at,
    updated_at
)
SELECT
    mii.stock_item_id,
    mii.menu_item_id,
    NULL,
    CASE
        WHEN mii.quantity_required > 0
            THEN ROUND((1.0 / mii.quantity_required)::numeric, 4)
        ELSE 1
    END,
    CASE
        WHEN mii.quantity_required > 0 THEN mii.quantity_required
        ELSE 1
    END,
    COALESCE(mii.created_at, CURRENT_TIMESTAMP),
    COALESCE(mii.updated_at, CURRENT_TIMESTAMP)
FROM menu_item_ingredients mii
WHERE mii.quantity_required IS NOT NULL
  AND mii.quantity_required > 0;

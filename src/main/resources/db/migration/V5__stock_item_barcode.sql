-- Barcode + qty added per scan (USB scanners act as keyboard wedges)

ALTER TABLE stock_items
    ADD COLUMN IF NOT EXISTS barcode VARCHAR(100),
    ADD COLUMN IF NOT EXISTS scan_pack_qty DECIMAL(12, 4) NOT NULL DEFAULT 1;

CREATE UNIQUE INDEX IF NOT EXISTS uk_stock_items_shop_barcode
    ON stock_items (shop_id, barcode)
    WHERE barcode IS NOT NULL AND barcode <> '';

COMMENT ON COLUMN stock_items.barcode IS 'Optional product barcode for scanner restock';
COMMENT ON COLUMN stock_items.scan_pack_qty IS 'Quantity added to the restock form per successful scan';

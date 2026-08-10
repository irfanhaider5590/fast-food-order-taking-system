/**
 * Stock management models and interfaces
 */

export interface StockItem {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  unit?: string;
  currentQuantity: number;
  minThreshold: number;
  isActive?: boolean;
  isLowStock?: boolean;
  barcode?: string | null;
  /** Qty added to restock form per barcode scan */
  scanPackQty?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface StockWarning {
  id?: number;
  stockItemId: number;
  stockItemNameEn?: string;
  stockItemNameUr?: string;
  warningMessageEn: string;
  warningMessageUr: string;
  currentQuantity: number;
  thresholdQuantity: number;
  isAcknowledged?: boolean;
  createdAt?: string;
}

/** Yield config: 1 stock unit covers servingsPerUnit of a menu product */
export interface StockConsumptionRow {
  id?: number;
  menuItemId: number;
  menuItemNameEn?: string;
  categoryId?: number;
  categoryNameEn?: string;
  sizeCode?: string | null;
  sizeNameEn?: string | null;
  servingsPerUnit: number;
  quantityPerServing?: number;
}

export interface StockConsumptionConfig {
  stockItemId: number;
  stockItemNameEn: string;
  stockItemNameUr?: string;
  unit: string;
  currentQuantity: number;
  minThreshold: number;
  rows: StockConsumptionRow[];
}

export interface ConsumptionCatalogSize {
  sizeCode: string;
  sizeNameEn: string;
}

export interface ConsumptionCatalogItem {
  id: number;
  nameEn: string;
  categoryId: number;
  sizes: ConsumptionCatalogSize[];
}

export interface ConsumptionCatalogCategory {
  id: number;
  nameEn: string;
  items: ConsumptionCatalogItem[];
}

export interface ConsumptionCatalog {
  categories: ConsumptionCatalogCategory[];
}


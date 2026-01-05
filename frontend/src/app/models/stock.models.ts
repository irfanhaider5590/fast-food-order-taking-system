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
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuItemIngredient {
  id?: number;
  stockItemId: number;
  stockItemNameEn?: string;
  stockItemNameUr?: string;
  stockItemUnit?: string;
  quantityRequired: number;
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


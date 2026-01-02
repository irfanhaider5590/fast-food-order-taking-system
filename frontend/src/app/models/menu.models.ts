/**
 * Menu-related models and interfaces
 */

export interface MenuCategory {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  displayOrder?: number;
  imageUrl?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuItemSize {
  id?: number;
  sizeCode: string;
  sizeNameEn: string;
  sizeNameUr?: string;
  priceModifier?: number;
  isAvailable?: boolean;
  displayOrder?: number;
}

export interface AddOn {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  price?: number;
  isAvailable?: boolean;
  displayOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuItem {
  id?: number;
  categoryId: number;
  categoryName?: string;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  basePrice: number;
  imageUrl?: string;
  isAvailable?: boolean;
  isCombo?: boolean;
  displayOrder?: number;
  sizes?: MenuItemSize[];
  availableAddOns?: AddOn[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ComboItem {
  id?: number;
  menuItemId: number;
  menuItemName?: string;
  quantity: number;
  displayOrder?: number;
}

export interface Combo {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  comboPrice: number;
  imageUrl?: string;
  isAvailable?: boolean;
  displayOrder?: number;
  items?: ComboItem[];
  createdAt?: string;
  updatedAt?: string;
}


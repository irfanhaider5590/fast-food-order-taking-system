/**
 * Order-related models and interfaces
 */

import { MenuItemSize } from './menu.models';

export interface CartItem {
  menuItemId: number;
  quantity: number;
  itemName: string;
  price: number;
  selectedSize?: MenuItemSize;
  selectedSizeCode?: string;
  addOns?: string[];
}

export type OrderType = 'TAKEAWAY' | 'HOME_DELIVERY' | 'TABLE_PICKUP';
export type PaymentMethod = 'CASH_ON_SPOT' | 'CASH_ON_DELIVERY';

export enum OrderStatus {
  PENDING = 'PENDING',
  PREPARING = 'PREPARING',
  READY = 'READY',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface OrderRequest {
  branchId: number;
  orderType: OrderType;
  customerName: string;
  customerPhone?: string | null;
  customerAddress?: string | null;
  tableNumber?: string | null;
  paymentMethod: PaymentMethod;
  voucherCode?: string | null;
  items: OrderItemRequest[];
}

export interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
  size?: string | null;
  addOnIds?: number[];
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  branchId: number;
  branchName?: string;
  orderType: OrderType;
  tableNumber?: string;
  customerName: string;
  customerPhone?: string;
  deliveryAddress?: string;
  paymentMethod?: PaymentMethod;
  paymentStatus?: string;
  orderStatus: OrderStatus;
  subtotal?: number;
  discountAmount?: number;
  voucherCode?: string;
  totalAmount: number;
  notes?: string;
  orderDate?: string;
  completedAt?: string;
  createdAt?: string;
  items?: OrderItemResponse[];
  stockWarnings?: StockWarning[];
}

export interface StockWarning {
  stockItemId: number;
  stockItemNameEn?: string;
  stockItemNameUr?: string;
  warningMessageEn: string;
  warningMessageUr: string;
  currentQuantity: number;
  thresholdQuantity: number;
  isAcknowledged?: boolean;
}

export interface OrderItemResponse {
  id: number;
  menuItemId?: number;
  comboId?: number;
  itemNameEn: string;
  itemNameUr?: string;
  sizeCode?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
}


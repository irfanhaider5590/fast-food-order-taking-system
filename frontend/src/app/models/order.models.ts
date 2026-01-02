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
  orderType: OrderType;
  customerName: string;
  totalAmount: number;
  status: string;
  createdAt: string;
}


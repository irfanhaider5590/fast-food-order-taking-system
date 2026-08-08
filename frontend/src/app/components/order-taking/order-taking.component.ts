import { Component, OnInit, ChangeDetectorRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuService } from '../../services/menu.service';
import { MenuCategory, MenuItem, MenuItemSize, AddOn } from '../../models/menu.models';
import { OrderService } from '../../services/order.service';
import { OrderResponse, OrderStatus } from '../../models/order.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';
import { SettingsService } from '../../services/settings.service';
import { Settings } from '../../models/settings.models';
import { OrderListCompactComponent } from '../order-list/order-list-compact.component';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface CartItem {
  menuItemId: number;
  quantity: number;
  itemName: string;
  price: number;
  selectedSize?: MenuItemSize;
  selectedSizeCode?: string;
  addOnIds: number[];
  addOnNames: string[];
}

@Component({
  selector: 'app-order-taking',
  standalone: true,
  imports: [CommonModule, FormsModule, OrderListCompactComponent],
  templateUrl: './order-taking.component.html',
  styleUrls: ['./order-taking.component.css']
})
export class OrderTakingComponent implements OnInit, AfterViewInit {

  menuCategories: MenuCategory[] = [];
  menuItems: MenuItem[] = [];
  filteredItems: MenuItem[] = [];
  selectedCategory: MenuCategory | null = null;
  cart: CartItem[] = [];
  orderType: string = 'TAKEAWAY';
  customerName: string = '';
  customerPhone: string = '';
  customerAddress: string = '';
  tableNumber: string = '';
  voucherCode: string = '';
  searchQuery: string = '';
  loadingCategories = false;
  loadingItems = false;
  placingOrder = false;
  branchId = 1;

  // Item compose modal (size + add-ons)
  composeItem: MenuItem | null = null;
  selectedSize: MenuItemSize | null = null;
  selectedAddOnIds: number[] = [];

  selectedOrderForStatus: OrderResponse | null = null;
  showStatusModal = false;
  updatingStatus = false;
  orderStatuses = Object.values(OrderStatus);

  @ViewChild(OrderListCompactComponent, { static: false }) orderListComponent?: OrderListCompactComponent;

  constructor(
    private menuService: MenuService,
    private orderService: OrderService,
    private cdr: ChangeDetectorRef,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private settingsService: SettingsService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.resolveBranchId();
    this.loadMenuCategories();
  }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  private resolveBranchId(): void {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      this.branchId = user?.branchId || 1;
    } catch {
      this.branchId = 1;
    }
  }

  loadMenuCategories() {
    this.loadingCategories = true;
    this.menuService.getCategories(true).subscribe({
      next: (categories) => {
        this.menuCategories = categories || [];
        this.loadingCategories = false;
        this.cdr.detectChanges();
        if (this.menuCategories.length > 0 && !this.selectedCategory) {
          this.selectCategory(this.menuCategories[0]);
        }
      },
      error: () => {
        this.loadingCategories = false;
        this.notificationService.showError('Failed to load categories');
        this.cdr.detectChanges();
      }
    });
  }

  selectCategory(category: MenuCategory) {
    if (!category?.id) return;
    this.selectedCategory = category;
    this.searchQuery = '';
    this.menuItems = [];
    this.filteredItems = [];
    this.loadMenuItems(category.id);
  }

  loadMenuItems(categoryId: number) {
    this.loadingItems = true;
    this.menuService.getMenuItems(categoryId).subscribe({
      next: (items) => {
        this.menuItems = (items || []).filter(item =>
          item.isAvailable && item.categoryId === categoryId
        );
        this.applySearch();
        this.loadingItems = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Error loading menu items:', err);
        this.loadingItems = false;
        this.notificationService.showError('Failed to load menu items');
        this.cdr.detectChanges();
      }
    });
  }

  applySearch(): void {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) {
      this.filteredItems = [...this.menuItems];
      return;
    }
    this.filteredItems = this.menuItems.filter(item =>
      item.nameEn?.toLowerCase().includes(q) ||
      item.nameUr?.toLowerCase().includes(q) ||
      item.descriptionEn?.toLowerCase().includes(q)
    );
  }

  getItemImage(item: MenuItem): string {
    if (item.imageUrl) {
      if (item.imageUrl.startsWith('http') || item.imageUrl.startsWith('/api/')) {
        return item.imageUrl.startsWith('/api/')
          ? `${environment.apiUrl.replace('/api', '')}${item.imageUrl}`
          : item.imageUrl;
      }
      return `${environment.apiUrl.replace('/api', '')}/api/files/serve?path=${encodeURIComponent(item.imageUrl)}`;
    }
    return '';
  }

  openCompose(item: MenuItem) {
    this.composeItem = item;
    this.selectedSize = null;
    this.selectedAddOnIds = [];
    const sizes = this.getAvailableSizes(item);
    if (sizes.length === 1) {
      this.selectedSize = sizes[0];
    }
  }

  closeCompose() {
    this.composeItem = null;
    this.selectedSize = null;
    this.selectedAddOnIds = [];
  }

  toggleAddOn(addOn: AddOn) {
    if (!addOn.id) return;
    const idx = this.selectedAddOnIds.indexOf(addOn.id);
    if (idx >= 0) {
      this.selectedAddOnIds.splice(idx, 1);
    } else {
      this.selectedAddOnIds.push(addOn.id);
    }
  }

  isAddOnSelected(addOn: AddOn): boolean {
    return !!addOn.id && this.selectedAddOnIds.includes(addOn.id);
  }

  getComposeUnitPrice(): number {
    if (!this.composeItem) return 0;
    const sizeMod = this.selectedSize?.priceModifier || 0;
    const addOns = (this.composeItem.availableAddOns || [])
      .filter(a => a.id && this.selectedAddOnIds.includes(a.id))
      .reduce((sum, a) => sum + (a.price || 0), 0);
    return this.composeItem.basePrice + sizeMod + addOns;
  }

  confirmCompose() {
    if (!this.composeItem) return;
    const sizes = this.getAvailableSizes(this.composeItem);
    if (sizes.length > 0 && !this.selectedSize) {
      this.notificationService.showError('Please select a size');
      return;
    }
    this.addItemToCart(this.composeItem, this.selectedSize, [...this.selectedAddOnIds]);
    this.closeCompose();
  }

  addItemToCart(item: MenuItem, selectedSize: MenuItemSize | null, addOnIds: number[]) {
    const sizeCode = selectedSize?.sizeCode || undefined;
    const sizePriceModifier = selectedSize?.priceModifier || 0;
    const selectedAddOns = (item.availableAddOns || []).filter(a => a.id && addOnIds.includes(a.id));
    const addOnTotal = selectedAddOns.reduce((sum, a) => sum + (a.price || 0), 0);
    const finalPrice = item.basePrice + sizePriceModifier + addOnTotal;
    const addOnKey = [...addOnIds].sort().join(',');

    const existingItem = this.cart.find(c =>
      c.menuItemId === item.id &&
      c.selectedSizeCode === sizeCode &&
      [...c.addOnIds].sort().join(',') === addOnKey
    );

    if (existingItem) {
      existingItem.quantity++;
    } else {
      this.cart.push({
        menuItemId: item.id!,
        quantity: 1,
        itemName: item.nameEn + (selectedSize ? ` (${selectedSize.sizeNameEn})` : ''),
        price: finalPrice,
        selectedSize: selectedSize || undefined,
        selectedSizeCode: sizeCode,
        addOnIds: [...addOnIds],
        addOnNames: selectedAddOns.map(a => a.nameEn)
      });
    }
  }

  getAvailableSizes(item: MenuItem): MenuItemSize[] {
    if (!item.sizes) return [];
    return item.sizes.filter((s: MenuItemSize) => s.isAvailable);
  }

  getAvailableAddOns(item: MenuItem): AddOn[] {
    return (item.availableAddOns || []).filter(a => a.isAvailable !== false);
  }

  updateQuantity(index: number, change: number) {
    const item = this.cart[index];
    item.quantity = Math.max(1, item.quantity + change);
  }

  removeFromCart(index: number) {
    this.cart.splice(index, 1);
  }

  getTotal() {
    return this.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  }

  placeOrder() {
    if (this.cart.length === 0) {
      this.notificationService.showError('Please add items to cart');
      return;
    }

    if (this.orderType === 'HOME_DELIVERY' && !this.customerAddress.trim()) {
      this.notificationService.showError('Please enter delivery address');
      return;
    }

    if (this.orderType === 'TABLE_PICKUP' && !this.tableNumber.trim()) {
      this.notificationService.showError('Please enter table number');
      return;
    }

    this.placingOrder = true;
    const orderData = {
      branchId: this.branchId,
      orderType: this.orderType,
      customerName: this.customerName || 'Walk-in Customer',
      customerPhone: this.customerPhone || null,
      deliveryAddress: this.orderType === 'HOME_DELIVERY' ? this.customerAddress : null,
      tableNumber: this.orderType === 'TABLE_PICKUP' ? this.tableNumber : null,
      paymentMethod: this.orderType === 'HOME_DELIVERY' ? 'CASH_ON_DELIVERY' : 'CASH_ON_SPOT',
      voucherCode: this.voucherCode || null,
      items: this.cart.map(item => ({
        menuItemId: item.menuItemId,
        quantity: item.quantity,
        sizeCode: item.selectedSizeCode || null,
        addOnIds: item.addOnIds || []
      }))
    };

    this.orderService.createOrder(orderData).subscribe({
      next: (response) => {
        this.placingOrder = false;
        this.notificationService.showSuccess(
          `Order placed: ${response.orderNumber}`,
          4000
        );

        if (response.stockWarnings?.length) {
          response.stockWarnings.forEach(warning => {
            this.notificationService.showWarning(
              `${warning.warningMessageEn}`,
              8000
            );
          });
        }

        this.checkAndPrintReceipt(response);
        this.resetOrderForm();
        setTimeout(() => this.orderListComponent?.loadOrders(), 400);
      },
      error: (err) => {
        this.placingOrder = false;
        this.logger.error('Error placing order:', err);
        this.notificationService.showError(
          'Error placing order: ' + (err.error?.message || 'Unknown error'),
          5000
        );
      }
    });
  }

  resetOrderForm(): void {
    this.cart = [];
    this.customerName = '';
    this.customerPhone = '';
    this.customerAddress = '';
    this.tableNumber = '';
    this.voucherCode = '';
    this.orderType = 'TAKEAWAY';
    this.closeCompose();
  }

  onOrderClick(order: OrderResponse): void {
    this.selectedOrderForStatus = order;
    this.showStatusModal = true;
  }

  updateOrderStatus(newStatus: OrderStatus): void {
    if (!this.selectedOrderForStatus) return;

    const orderId = this.selectedOrderForStatus.id;
    this.showStatusModal = false;
    this.selectedOrderForStatus = null;

    this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
      next: () => {
        this.notificationService.showSuccess(`Status updated to ${newStatus}`, 3000);
        setTimeout(() => this.orderListComponent?.loadOrders(), 300);
      },
      error: (err) => {
        this.notificationService.showError(
          `Failed to update status: ${err?.message || 'Unknown error'}`,
          4000
        );
        setTimeout(() => this.orderListComponent?.loadOrders(), 300);
      }
    });
  }

  closeStatusModal(): void {
    this.showStatusModal = false;
    this.selectedOrderForStatus = null;
    this.updatingStatus = false;
  }

  getStatusColor(status: OrderStatus): string {
    const colors: { [key: string]: string } = {
      'PENDING': '#f59e0b',
      'PREPARING': '#0ea5e9',
      'READY': '#2563eb',
      'COMPLETED': '#16a34a',
      'CANCELLED': '#dc2626'
    };
    return colors[status] || '#6b7280';
  }

  getStatusLabel(status: OrderStatus): string {
    const labels: { [key: string]: string } = {
      'PENDING': 'Pending',
      'PREPARING': 'Preparing',
      'READY': 'Ready',
      'COMPLETED': 'Completed',
      'CANCELLED': 'Cancelled'
    };
    return labels[status] || status;
  }

  checkAndPrintReceipt(order: OrderResponse): void {
    this.http.get<{autoPrintEnabled: boolean}>(`${environment.apiUrl}/receipt/auto-print-status`).subscribe({
      next: (status) => {
        if (status.autoPrintEnabled) {
          this.printReceipt(order);
        }
      },
      error: (err) => this.logger.error('Error checking auto-print status:', err)
    });
  }

  printReceipt(order: OrderResponse): void {
    this.settingsService.getSettings().subscribe({
      next: (settings: Settings) => {
        const receiptContent = this.generateReceiptHtml(order, settings);
        const printWindow = window.open('', '_blank', 'width=300,height=600');
        if (printWindow) {
          printWindow.document.write(receiptContent);
          printWindow.document.close();
          printWindow.focus();
          setTimeout(() => {
            printWindow.print();
            setTimeout(() => printWindow.close(), 500);
          }, 250);
        }
      },
      error: () => {
        const receiptContent = this.generateReceiptHtml(order, null);
        const printWindow = window.open('', '_blank', 'width=300,height=600');
        if (printWindow) {
          printWindow.document.write(receiptContent);
          printWindow.document.close();
          printWindow.focus();
          setTimeout(() => {
            printWindow.print();
            setTimeout(() => printWindow.close(), 500);
          }, 250);
        }
      }
    });
  }

  private generateReceiptHtml(order: OrderResponse, settings: Settings | null): string {
    const brandName = settings?.brandName || 'Order System';
    const brandLocation = settings?.address || '';
    const brandLogoUrl = settings?.brandLogoUrl || null;

    let html = `<!DOCTYPE html><html><head><meta charset="UTF-8"><style>
      @page { size: 80mm auto; margin: 5mm; }
      body { font-family: Arial, sans-serif; margin: 0; padding: 10px; width: 70mm; font-size: 12px; }
      .header { text-align: center; margin-bottom: 15px; }
      .logo-img { max-width: 60mm; max-height: 30mm; margin: 0 auto 8px; display: block; }
      .logo { font-size: 18px; font-weight: bold; }
      .items { width: 100%; border-collapse: collapse; margin: 10px 0; }
      .items th, .items td { padding: 4px 2px; text-align: left; border-bottom: 1px solid #ddd; font-size: 10px; }
      .total { font-weight: bold; font-size: 12px; margin-top: 8px; }
      .footer { text-align: center; margin-top: 15px; color: #666; font-size: 10px; }
    </style></head><body>
    <div class="header">
      ${brandLogoUrl ? `<img src="${this.getImageUrlForReceipt(brandLogoUrl)}" alt="Logo" class="logo-img" />` : ''}
      <div class="logo">${brandName}</div>
      <div>${brandLocation}</div>
    </div>
    <div>
      <strong>Order:</strong> ${order.orderNumber}<br>
      <strong>Date:</strong> ${order.orderDate ? new Date(order.orderDate).toLocaleString() : ''}<br>
      <strong>Type:</strong> ${order.orderType}<br>
      ${order.customerName ? `<strong>Customer:</strong> ${order.customerName}<br>` : ''}
      ${order.customerPhone ? `<strong>Phone:</strong> ${order.customerPhone}<br>` : ''}
      ${order.tableNumber ? `<strong>Table:</strong> ${order.tableNumber}<br>` : ''}
      ${order.deliveryAddress ? `<strong>Address:</strong> ${order.deliveryAddress}<br>` : ''}
    </div>
    <table class="items"><tr><th>Item</th><th>Qty</th><th>Price</th></tr>`;

    (order.items || []).forEach(item => {
      html += `<tr><td>${item.itemNameEn}</td><td>${item.quantity}</td><td>${item.totalPrice.toFixed(2)}</td></tr>`;
    });

    html += `</table><div>
      <strong>Subtotal:</strong> ${(order.subtotal || 0).toFixed(2)}<br>
      ${(order.discountAmount && order.discountAmount > 0) ? `<strong>Discount:</strong> ${order.discountAmount.toFixed(2)}<br>` : ''}
      <div class="total">Total: ${(order.totalAmount || 0).toFixed(2)}</div>
    </div>
    <div class="footer">Thank you!</div></body></html>`;
    return html;
  }

  private getImageUrlForReceipt(path: string | null | undefined): string {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://')) return path;
    const backendBaseUrl = environment.apiUrl.replace('/api', '');
    if (path.startsWith('/api/files/serve')) return `${backendBaseUrl}${path}`;
    return `${backendBaseUrl}/api/files/serve?path=${encodeURIComponent(path)}`;
  }
}

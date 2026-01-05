import { Component, OnInit, ChangeDetectorRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MenuService } from '../../services/menu.service';
import { MenuCategory, MenuItem, MenuItemSize } from '../../models/menu.models';
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
  addOns?: string[];
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
  selectedCategory: MenuCategory | null = null;
  cart: CartItem[] = [];
  orderType: string = 'TAKEAWAY';
  customerName: string = '';
  customerPhone: string = '';
  customerAddress: string = '';
  tableNumber: string = '';
  voucherCode: string = '';
  loadingCategories = false;
  loadingItems = false;
  selectedItemForSize: MenuItem | null = null;
  
  // Status update modal
  selectedOrderForStatus: OrderResponse | null = null;
  showStatusModal = false;
  updatingStatus = false;
  
  // Available statuses
  orderStatuses = Object.values(OrderStatus);

  @ViewChild(OrderListCompactComponent, { static: false }) orderListComponent?: OrderListCompactComponent;

  constructor(
    private menuService: MenuService,
    private orderService: OrderService,
    private cdr: ChangeDetectorRef,
    private logger: LoggerService,
    private router: Router,
    private notificationService: NotificationService,
    private settingsService: SettingsService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadMenuCategories();
  }

  ngAfterViewInit(): void {
    // Ensure ViewChild is initialized
    this.cdr.detectChanges();
    this.logger.debug('OrderTakingComponent.ngAfterViewInit() - OrderListComponent available:', !!this.orderListComponent);
    
    // Double check after a small delay to ensure ViewChild is properly initialized
    setTimeout(() => {
      this.cdr.detectChanges();
      this.logger.debug('OrderTakingComponent.ngAfterViewInit() - After delay, OrderListComponent available:', !!this.orderListComponent);
      if (this.orderListComponent) {
        this.logger.info('OrderListComponent successfully initialized and ready');
      }
    }, 100);
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
      error: (err) => {
        // Error handled by UI
        this.loadingCategories = false;
        this.cdr.detectChanges();
      }
    });
  }

  selectCategory(category: MenuCategory) {
    if (!category || !category.id) return;
    this.selectedCategory = category;
    this.menuItems = []; // Clear previous items
    this.loadMenuItems(category.id);
  }

  loadMenuItems(categoryId: number) {
    if (!categoryId) return;
    this.loadingItems = true;
    this.menuService.getMenuItems(categoryId).subscribe({
      next: (items) => {
        // Filter by category and availability
        this.menuItems = (items || []).filter(item => 
          item.isAvailable && item.categoryId === categoryId
        );
        this.loadingItems = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Error loading menu items:', err);
        this.loadingItems = false;
        this.cdr.detectChanges();
      }
    });
  }

  getItemImage(item: MenuItem): string {
    if (item.imageUrl) {
      return item.imageUrl;
    }
    // Default images based on category name
    const categoryName = item.categoryName?.toLowerCase() || '';
    if (categoryName.includes('burger')) {
      return 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop';
    } else if (categoryName.includes('pizza')) {
      return 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&h=300&fit=crop';
    } else if (categoryName.includes('fries') || categoryName.includes('fry')) {
      return 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&h=300&fit=crop';
    } else if (categoryName.includes('drink')) {
      return 'https://images.unsplash.com/photo-1544145945-f90425340c7e?w=400&h=300&fit=crop';
    } else if (categoryName.includes('combo') || categoryName.includes('deal')) {
      return 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop';
    }
    return 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop';
  }

  addToCart(item: MenuItem) {
    // If item has sizes, show size selection modal
    if (item.sizes && item.sizes.length > 0 && item.sizes.some((s: MenuItemSize) => s.isAvailable)) {
      this.selectedItemForSize = item;
      return;
    }
    
    // Add directly if no sizes
    this.addItemToCart(item, null);
  }

  addItemToCart(item: MenuItem, selectedSize: MenuItemSize | null) {
    const sizeCode = selectedSize?.sizeCode || null;
    const sizePriceModifier = selectedSize?.priceModifier || 0;
    const finalPrice = item.basePrice + (sizePriceModifier || 0);
    
    // Check if same item with same size already in cart
    const existingItem = this.cart.find(c => 
      c.menuItemId === item.id && c.selectedSizeCode === sizeCode
    );
    
    if (existingItem) {
      existingItem.quantity++;
    } else {
      const cartItem: CartItem = {
        menuItemId: item.id!,
        quantity: 1,
        itemName: item.nameEn + (selectedSize ? ` (${selectedSize.sizeNameEn})` : ''),
        price: finalPrice,
        selectedSize: selectedSize || undefined,
        selectedSizeCode: sizeCode || undefined
      };
      this.cart.push(cartItem);
    }
    
    this.selectedItemForSize = null;
  }

  closeSizeModal() {
    this.selectedItemForSize = null;
  }

  getAvailableSizes(item: MenuItem): MenuItemSize[] {
    if (!item.sizes) return [];
    return item.sizes.filter((s: MenuItemSize) => s.isAvailable);
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
      alert('Please add items to cart');
      return;
    }

    if (this.orderType === 'HOME_DELIVERY' && !this.customerAddress) {
      alert('Please enter delivery address');
      return;
    }

    if (this.orderType === 'TABLE_PICKUP' && !this.tableNumber) {
      alert('Please enter table number');
      return;
    }

    const token = localStorage.getItem('accessToken');
    const orderData = {
      branchId: 1,
      orderType: this.orderType,
      customerName: this.customerName || 'Walk-in Customer',
      customerPhone: this.customerPhone || null,
      customerAddress: this.orderType === 'HOME_DELIVERY' ? this.customerAddress : null,
      tableNumber: this.orderType === 'TABLE_PICKUP' ? this.tableNumber : null,
      paymentMethod: 'CASH_ON_SPOT',
      voucherCode: this.voucherCode || null,
      items: this.cart.map(item => ({
        menuItemId: item.menuItemId,
        quantity: item.quantity,
        size: item.selectedSizeCode || null,
        addOnIds: []
      }))
    };

    this.orderService.createOrder(orderData).subscribe({
      next: (response) => {
        this.logger.info(`Order placed successfully: ${response.orderNumber}`);
        
        // Show success notification
        this.notificationService.showSuccess(
          `Order placed successfully! Order Number: ${response.orderNumber}`,
          4000
        );
        
        // Show stock warnings if any
        if (response.stockWarnings && response.stockWarnings.length > 0) {
          response.stockWarnings.forEach(warning => {
            const message = `${warning.warningMessageEn}\n${warning.warningMessageUr}`;
            this.notificationService.showWarning(message, 8000);
          });
        }
        
        // Print receipt if enabled (check setting first)
        this.checkAndPrintReceipt(response);
        
        // Reset form first
        this.resetOrderForm();
        
        // Refresh order list after a small delay to ensure backend has processed the order
        setTimeout(() => {
          this.logger.debug('Attempting to refresh order list after order placement');
          this.cdr.detectChanges(); // Force change detection first
          
          if (this.orderListComponent) {
            this.logger.debug('OrderListComponent found, calling loadOrders()');
            this.orderListComponent.loadOrders();
          } else {
            this.logger.warn('OrderListComponent not found, trying again...');
            // Try again after another small delay
            setTimeout(() => {
              this.cdr.detectChanges();
              if (this.orderListComponent) {
                this.logger.debug('OrderListComponent found on retry, calling loadOrders()');
                this.orderListComponent.loadOrders();
              } else {
                this.logger.error('OrderListComponent still not found after retry');
              }
            }, 200);
          }
        }, 800); // Increased delay to ensure backend has saved the order
      },
      error: (err) => {
        this.logger.error('Error placing order:', err);
        this.notificationService.showError(
          'Error placing order: ' + (err.error?.message || 'Unknown error'),
          5000
        );
      }
    });
  }

  resetOrderForm(): void {
    // Clear cart
    this.cart = [];
    
    // Reset customer info
    this.customerName = '';
    this.customerPhone = '';
    this.customerAddress = '';
    this.tableNumber = '';
    this.voucherCode = '';
    
    // Reset order type to default
    this.orderType = 'TAKEAWAY';
    
    // Close any open modals
    this.selectedItemForSize = null;
    
    this.logger.info('Order form reset');
  }

  onOrderClick(order: OrderResponse): void {
    // Show status update modal instead of navigating
    this.selectedOrderForStatus = order;
    this.showStatusModal = true;
    this.logger.info(`Order clicked: ${order.orderNumber}, current status: ${order.orderStatus}`);
  }

  updateOrderStatus(newStatus: OrderStatus): void {
    if (!this.selectedOrderForStatus) return;
    
    const orderId = this.selectedOrderForStatus.id;
    const orderNumber = this.selectedOrderForStatus.orderNumber;
    
    // Close modal immediately for better UX
    this.showStatusModal = false;
    this.selectedOrderForStatus = null;
    this.updatingStatus = false;
    
    this.logger.info(`Updating order ${orderNumber} status to ${newStatus}`);
    
    // Update status in background
    this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
      next: (updatedOrder) => {
        this.logger.info(`Order ${updatedOrder.orderNumber} status updated successfully to ${newStatus}`);
        
        // Show success notification
        this.notificationService.showSuccess(
          `Order status updated to ${newStatus}`,
          3000
        );
        
        // Refresh order list after successful update
        setTimeout(() => {
          this.logger.debug('Attempting to refresh order list after status update');
          this.cdr.detectChanges(); // Force change detection first
          
          if (this.orderListComponent) {
            this.logger.debug('OrderListComponent found, calling loadOrders()');
            this.orderListComponent.loadOrders();
          } else {
            this.logger.warn('OrderListComponent not found, trying again...');
            // Try again after another small delay
            setTimeout(() => {
              this.cdr.detectChanges();
              if (this.orderListComponent) {
                this.logger.debug('OrderListComponent found on retry, calling loadOrders()');
                this.orderListComponent.loadOrders();
              } else {
                this.logger.error('OrderListComponent still not found after retry');
              }
            }, 200);
          }
        }, 600); // Delay to ensure backend has processed the update
      },
      error: (err) => {
        this.logger.error('Error updating order status:', err);
        this.notificationService.showError(
          `Failed to update order status: ${err?.message || 'Unknown error'}`,
          4000
        );
        
        // Refresh order list even on error to show current state
        setTimeout(() => {
          this.cdr.detectChanges();
          if (this.orderListComponent) {
            this.orderListComponent.loadOrders();
          }
        }, 600);
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
      'PENDING': '#ffc107',
      'PREPARING': '#17a2b8',
      'READY': '#007bff',
      'COMPLETED': '#28a745',
      'CANCELLED': '#dc3545'
    };
    return colors[status] || '#6c757d';
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
    // Check if auto-print is enabled
    this.http.get<{autoPrintEnabled: boolean}>(`${environment.apiUrl}/receipt/auto-print-status`).subscribe({
      next: (status) => {
        if (status.autoPrintEnabled) {
          this.printReceipt(order);
        } else {
          this.logger.debug('Auto-print is disabled, skipping receipt printing');
        }
      },
      error: (err) => {
        this.logger.error('Error checking auto-print status:', err);
        // Don't print if we can't verify the setting
      }
    });
  }

  printReceipt(order: OrderResponse): void {
    // Fetch settings first, then generate receipt
    this.settingsService.getSettings().subscribe({
      next: (settings: Settings) => {
        try {
          const receiptContent = this.generateReceiptHtml(order, settings);
          const printWindow = window.open('', '_blank', 'width=300,height=600');
          if (printWindow) {
            printWindow.document.write(receiptContent);
            printWindow.document.close();
            printWindow.focus();
            setTimeout(() => {
              printWindow.print();
              // Auto-close after printing
              setTimeout(() => {
                printWindow.close();
              }, 500);
            }, 250);
          }
        } catch (error) {
          this.logger.error('Error printing receipt:', error);
        }
      },
      error: (err: any) => {
        this.logger.error('Error fetching settings for receipt:', err);
        // Use defaults if settings fetch fails
        try {
          const receiptContent = this.generateReceiptHtml(order, null);
          const printWindow = window.open('', '_blank', 'width=300,height=600');
          if (printWindow) {
            printWindow.document.write(receiptContent);
            printWindow.document.close();
            printWindow.focus();
            setTimeout(() => {
              printWindow.print();
              // Auto-close after printing
              setTimeout(() => {
                printWindow.close();
              }, 500);
            }, 250);
          }
        } catch (error) {
          this.logger.error('Error printing receipt:', error);
        }
      }
    });
  }

  private generateReceiptHtml(order: OrderResponse, settings: Settings | null): string {
    // Get brand info from settings or use defaults
    const brandName = settings?.brandName || 'Fast Food Express';
    const brandLocation = settings?.address || 'Gujranwala, Pakistan';
    const brandLogoUrl = settings?.brandLogoUrl || null;
    
    let html = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    @page {
      size: 80mm auto;
      margin: 5mm;
    }
    body { 
      font-family: Arial, sans-serif; 
      margin: 0;
      padding: 10px;
      width: 70mm;
      font-size: 12px;
    }
    .header { text-align: center; margin-bottom: 15px; }
    .logo-img { max-width: 60mm; max-height: 30mm; margin-bottom: 8px; display: block; margin-left: auto; margin-right: auto; }
    .logo { font-size: 18px; font-weight: bold; margin-bottom: 3px; }
    .location { font-size: 11px; color: #666; }
    .section { margin: 10px 0; font-size: 11px; }
    .items { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 11px; }
    .items th, .items td { padding: 4px 2px; text-align: left; border-bottom: 1px solid #ddd; }
    .items th { background-color: #f5f5f5; font-size: 10px; }
    .items td { font-size: 10px; }
    .total { font-weight: bold; font-size: 12px; margin-top: 8px; }
    .footer { text-align: center; margin-top: 15px; color: #666; font-size: 10px; }
    @media print { 
      body { margin: 0; padding: 5mm; }
      .no-print { display: none; }
    }
    @media screen {
      body { margin: 10px auto; }
    }
  </style>
</head>
<body>
  <div class="header">
    ${brandLogoUrl ? `<img src="${this.getImageUrlForReceipt(brandLogoUrl)}" alt="Logo" class="logo-img" />` : ''}
    <div class="logo">${brandName}</div>
    <div class="location">${brandLocation}</div>
  </div>
  
  <div class="section">
    <strong>Order Number:</strong> ${order.orderNumber}<br>
    <strong>Date:</strong> ${order.orderDate ? new Date(order.orderDate).toLocaleString() : ''}<br>
    <strong>Type:</strong> ${order.orderType}<br>`;
    
    if (order.customerName) {
      html += `<strong>Customer:</strong> ${order.customerName}<br>`;
    }
    if (order.customerPhone) {
      html += `<strong>Phone:</strong> ${order.customerPhone}<br>`;
    }
    if (order.tableNumber) {
      html += `<strong>Table:</strong> ${order.tableNumber}<br>`;
    }
    if (order.deliveryAddress) {
      html += `<strong>Address:</strong> ${order.deliveryAddress}<br>`;
    }
    
    html += `</div>
  
  <table class="items">
    <tr><th>Item</th><th>Qty</th><th>Price</th></tr>`;
    
    if (order.items) {
      order.items.forEach(item => {
        html += `<tr>
          <td>${item.itemNameEn}</td>
          <td>${item.quantity}</td>
          <td>${item.totalPrice.toFixed(2)}</td>
        </tr>`;
      });
    }
    
    html += `</table>
  
  <div class="section">
    <strong>Subtotal:</strong> ${(order.subtotal || 0).toFixed(2)}<br>`;
    
    if (order.discountAmount && order.discountAmount > 0) {
      html += `<strong>Discount:</strong> ${order.discountAmount.toFixed(2)}<br>`;
      if (order.voucherCode) {
        html += `<strong>Voucher:</strong> ${order.voucherCode}<br>`;
      }
    }
    
    html += `<div class="total">Total: ${(order.totalAmount || 0).toFixed(2)}</div>
    <strong>Payment:</strong> ${order.paymentMethod}<br>
    <strong>Status:</strong> ${order.orderStatus}<br>
  </div>
  
  <div class="footer">
    Thank you for your order!
  </div>
</body>
</html>`;
    
    return html;
  }

  private getImageUrlForReceipt(path: string | null | undefined): string {
    if (!path) return '';
    
    // If path already starts with http:// or https://, return as is
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path;
    }
    
    // Backend base URL (without /api suffix)
    const backendBaseUrl = window.location.origin + '/fast-food-order-api';
    
    // If path starts with /api/files/serve, prepend backend URL
    if (path.startsWith('/api/files/serve')) {
      return `${backendBaseUrl}${path}`;
    }
    
    // If path starts with /assets/images/, convert to API endpoint
    if (path.startsWith('/assets/images/')) {
      return `${backendBaseUrl}/api/files/serve?path=${encodeURIComponent(path)}`;
    }
    
    // Default: assume it's an assets path
    return `${backendBaseUrl}/api/files/serve?path=${encodeURIComponent(path)}`;
  }
}

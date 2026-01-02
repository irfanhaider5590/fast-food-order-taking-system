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
import { OrderListCompactComponent } from '../order-list/order-list-compact.component';

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
    private notificationService: NotificationService
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
}

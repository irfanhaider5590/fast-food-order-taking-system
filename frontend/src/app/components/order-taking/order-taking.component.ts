import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuService } from '../../services/menu.service';
import { MenuCategory, MenuItem, MenuItemSize } from '../../models/menu.models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoggerService } from '../../services/logger.service';

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
  imports: [CommonModule, FormsModule],
  templateUrl: './order-taking.component.html',
  styleUrls: ['./order-taking.component.css']
})
export class OrderTakingComponent implements OnInit {
  
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

  constructor(
    private menuService: MenuService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private logger: LoggerService
  ) {}

  ngOnInit() {
    this.loadMenuCategories();
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

    this.http.post<any>(`${environment.apiUrl}/orders`, orderData, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }).subscribe({
      next: (response) => {
        alert(`Order placed successfully! Order Number: ${response.orderNumber}`);
        this.cart = [];
        this.customerName = '';
        this.customerPhone = '';
        this.customerAddress = '';
        this.tableNumber = '';
        this.voucherCode = '';
      },
      error: (err) => {
        this.logger.error('Error placing order:', err);
        alert('Error placing order: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }
}

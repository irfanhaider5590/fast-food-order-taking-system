import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService, OrderSearchParams, OrderSearchResponse } from '../../services/order.service';
import { OrderResponse, OrderStatus } from '../../models/order.models';
import { LoggerService } from '../../services/logger.service';
import { NavigationComponent } from '../../shared/components/navigation/navigation.component';

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [CommonModule, FormsModule, NavigationComponent],
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css']
})
export class OrderManagementComponent implements OnInit {
  orders: OrderResponse[] = [];
  loading = false;
  error: string | null = null;
  
  // Filters
  searchParams: OrderSearchParams = {
    page: 0,
    size: 10,
    branchId: 1
  };
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Status update modal
  selectedOrder: OrderResponse | null = null;
  showStatusModal = false;
  newStatus: OrderStatus = OrderStatus.PENDING;
  
  // Available statuses
  orderStatuses = Object.values(OrderStatus);

  constructor(
    private orderService: OrderService,
    private logger: LoggerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.logger.info('OrderManagementComponent.ngOnInit() - Component initialized');
    this.logger.debug('OrderManagementComponent.ngOnInit() - Initial searchParams:', this.searchParams);
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;
    
    this.searchParams.page = this.currentPage;
    this.searchParams.size = this.pageSize;
    
    this.logger.info('OrderManagementComponent.loadOrders() - Starting to load orders');
    this.logger.debug('OrderManagementComponent.loadOrders() - Search params:', this.searchParams);
    
    this.orderService.searchOrders(this.searchParams).subscribe({
      next: (response: OrderSearchResponse) => {
        this.logger.debug('OrderManagementComponent.loadOrders() - Response received:', JSON.stringify(response));
        this.logger.debug('OrderManagementComponent.loadOrders() - Response content:', response.content);
        this.logger.debug('OrderManagementComponent.loadOrders() - Response totalElements:', response.totalElements);
        this.logger.debug('OrderManagementComponent.loadOrders() - Response totalPages:', response.totalPages);
        this.logger.debug('OrderManagementComponent.loadOrders() - Response number:', response.number);
        
        this.orders = response.content || [];
        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.currentPage = response.number !== undefined ? response.number : 0;
        this.loading = false;
        
        this.logger.info(`OrderManagementComponent.loadOrders() - Successfully loaded ${this.orders.length} orders (page ${this.currentPage + 1} of ${this.totalPages}, total: ${this.totalElements})`);
        this.logger.debug('OrderManagementComponent.loadOrders() - Orders array:', this.orders);
        this.logger.debug('OrderManagementComponent.loadOrders() - Loading state:', this.loading);
        this.logger.debug('OrderManagementComponent.loadOrders() - Orders length:', this.orders.length);
        
        // Force change detection
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('OrderManagementComponent.loadOrders() - Error loading orders:', {
          error: err,
          status: err?.status,
          statusText: err?.statusText,
          message: err?.message,
          errorBody: err?.error
        });
        this.error = `Failed to load orders: ${err?.statusText || err?.message || 'Unknown error'}`;
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  onResetFilters(): void {
    this.searchParams = {
      page: 0,
      size: 10,
      branchId: 1
    };
    this.currentPage = 0;
    this.loadOrders();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadOrders();
  }

  onOrderClick(order: OrderResponse): void {
    this.selectedOrder = order;
    this.newStatus = order.orderStatus as OrderStatus;
    this.showStatusModal = true;
  }

  updateOrderStatus(): void {
    if (!this.selectedOrder) return;
    
    this.loading = true;
    this.orderService.updateOrderStatus(this.selectedOrder.id, this.newStatus).subscribe({
      next: (updatedOrder) => {
        this.logger.info(`Order ${updatedOrder.orderNumber} status updated to ${this.newStatus}`);
        this.showStatusModal = false;
        this.selectedOrder = null;
        this.loadOrders(); // Reload to show updated status
      },
      error: (err) => {
        this.logger.error('Error updating order status:', err);
        this.error = 'Failed to update order status';
        this.loading = false;
      }
    });
  }

  closeStatusModal(): void {
    this.showStatusModal = false;
    this.selectedOrder = null;
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': '#ffc107',
      'PREPARING': '#17a2b8',
      'READY': '#007bff',
      'COMPLETED': '#28a745',
      'CANCELLED': '#dc3545'
    };
    return colors[status] || '#6c757d';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}


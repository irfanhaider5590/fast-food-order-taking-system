import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { OrderResponse } from '../../models/order.models';
import { LoggerService } from '../../services/logger.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit, OnDestroy {
  orders: OrderResponse[] = [];
  loading = false;
  error: string | null = null;
  private refreshSubscription?: Subscription;
  private readonly REFRESH_INTERVAL = 5000; // 5 seconds

  constructor(
    private orderService: OrderService,
    private logger: LoggerService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.stopAutoRefresh();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;
    this.logger.info('OrderListComponent.loadOrders() - Starting to load orders');
    
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.logger.info(`OrderListComponent.loadOrders() - Received ${orders.length} orders from API`);
        
        // Sort by orderDate/createdAt descending (newest first)
        this.orders = orders.sort((a, b) => {
          const dateA = new Date(a.orderDate || a.createdAt || '').getTime();
          const dateB = new Date(b.orderDate || b.createdAt || '').getTime();
          return dateB - dateA;
        });
        
        this.loading = false;
        this.logger.debug(`OrderListComponent.loadOrders() - Successfully loaded and sorted ${this.orders.length} orders`);
      },
      error: (err) => {
        this.logger.error('OrderListComponent.loadOrders() - Error loading orders:', {
          error: err,
          status: err?.status,
          statusText: err?.statusText,
          message: err?.message,
          url: err?.url
        });
        this.error = `Failed to load orders: ${err?.statusText || err?.message || 'Unknown error'}`;
        this.loading = false;
      }
    });
  }

  startAutoRefresh(): void {
    this.logger.info(`OrderListComponent.startAutoRefresh() - Starting auto-refresh every ${this.REFRESH_INTERVAL/1000} seconds`);
    
    // Refresh every 5 seconds
    this.refreshSubscription = interval(this.REFRESH_INTERVAL)
      .pipe(
        switchMap(() => {
          this.logger.debug('OrderListComponent.startAutoRefresh() - Auto-refresh triggered');
          return this.orderService.getOrders();
        })
      )
      .subscribe({
        next: (orders) => {
          // Sort by orderDate/createdAt descending (newest first)
          this.orders = orders.sort((a, b) => {
            const dateA = new Date(a.orderDate || a.createdAt || '').getTime();
            const dateB = new Date(b.orderDate || b.createdAt || '').getTime();
            return dateB - dateA;
          });
          this.logger.debug(`OrderListComponent.startAutoRefresh() - Auto-refreshed: ${orders.length} orders`);
        },
        error: (err) => {
          this.logger.error('OrderListComponent.startAutoRefresh() - Error auto-refreshing orders:', {
            error: err,
            status: err?.status,
            statusText: err?.statusText,
            message: err?.message
          });
          // Don't show error on auto-refresh, just log it
        }
      });
  }

  stopAutoRefresh(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'Unknown';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) {
      return 'Just now';
    } else if (diffMins < 60) {
      return `${diffMins}m ago`;
    } else if (diffHours < 24) {
      return `${diffHours}h ago`;
    } else if (diffDays < 7) {
      return `${diffDays}d ago`;
    } else {
      return date.toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }

  getOrderTypeLabel(orderType: string): string {
    const labels: { [key: string]: string } = {
      'TAKEAWAY': 'Takeaway',
      'TABLE_PICKUP': 'Table',
      'HOME_DELIVERY': 'Delivery'
    };
    return labels[orderType] || orderType;
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': '#ffc107',
      'CONFIRMED': '#17a2b8',
      'PREPARING': '#007bff',
      'READY': '#28a745',
      'COMPLETED': '#6c757d',
      'CANCELLED': '#dc3545'
    };
    return colors[status] || '#6c757d';
  }

  getOrderTypeColor(orderType: string): string {
    const colors: { [key: string]: string } = {
      'TAKEAWAY': '#007bff',
      'TABLE_PICKUP': '#28a745',
      'HOME_DELIVERY': '#ffc107'
    };
    return colors[orderType] || '#6c757d';
  }
}


import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, catchError } from 'rxjs';
import { environment } from '../../environments/environment';
import { OrderResponse, OrderStatus } from '../models/order.models';
import { LoggerService } from './logger.service';

export interface OrderSearchParams {
  orderNumber?: string;
  customerName?: string;
  customerPhone?: string;
  startDate?: string;
  endDate?: string;
  branchId?: number;
  page?: number;
  size?: number;
}

export interface OrderSearchResponse {
  content: OrderResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(
    private http: HttpClient,
    private logger: LoggerService
  ) {
    this.logger.debug('OrderService initialized with API URL:', this.apiUrl);
  }

  /**
   * Get all orders
   */
  getOrders(): Observable<OrderResponse[]> {
    const token = localStorage.getItem('accessToken');
    this.logger.debug('OrderService.getOrders() - Calling:', this.apiUrl);
    this.logger.debug('OrderService.getOrders() - Token present:', !!token);
    
    return this.http.get<OrderResponse[]>(this.apiUrl, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).pipe(
      tap({
        next: (orders) => this.logger.info(`OrderService.getOrders() - Success: Received ${orders.length} orders`),
        error: (error) => this.logger.error('OrderService.getOrders() - Error:', error)
      }),
      catchError((error) => {
        this.logger.error('OrderService.getOrders() - Request failed:', {
          url: this.apiUrl,
          status: error.status,
          message: error.message,
          error: error.error
        });
        throw error;
      })
    );
  }

  /**
   * Get orders with pagination
   */
  getOrdersPaginated(page: number = 0, size: number = 50): Observable<any> {
    const token = localStorage.getItem('accessToken');
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(this.apiUrl, {
      params,
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  /**
   * Get order by ID
   */
  getOrderById(id: number): Observable<OrderResponse> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<OrderResponse>(`${this.apiUrl}/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  /**
   * Create new order
   */
  createOrder(orderData: any): Observable<OrderResponse> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<OrderResponse>(this.apiUrl, orderData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  /**
   * Update order status
   */
  updateOrderStatus(orderId: number, status: OrderStatus): Observable<OrderResponse> {
    const token = localStorage.getItem('accessToken');
    this.logger.info(`OrderService.updateOrderStatus() - Updating order ${orderId} to status: ${status}`);
    
    return this.http.patch<OrderResponse>(`${this.apiUrl}/${orderId}/status`, 
      { orderStatus: status },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    ).pipe(
      tap({
        next: () => this.logger.info(`OrderService.updateOrderStatus() - Success: Order ${orderId} status updated to ${status}`),
        error: (error) => this.logger.error('OrderService.updateOrderStatus() - Error:', error)
      })
    );
  }

  /**
   * Search orders with filters and pagination
   */
  searchOrders(params: OrderSearchParams): Observable<OrderSearchResponse> {
    const token = localStorage.getItem('accessToken');
    let httpParams = new HttpParams();
    
    if (params.orderNumber) httpParams = httpParams.set('orderNumber', params.orderNumber);
    if (params.customerName) httpParams = httpParams.set('customerName', params.customerName);
    if (params.customerPhone) httpParams = httpParams.set('customerPhone', params.customerPhone);
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.branchId) httpParams = httpParams.set('branchId', params.branchId.toString());
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    
    this.logger.debug('OrderService.searchOrders() - Searching with params:', params);
    this.logger.debug('OrderService.searchOrders() - Request URL:', `${this.apiUrl}/search`);
    this.logger.debug('OrderService.searchOrders() - Request params:', httpParams.toString());
    
    return this.http.get<OrderSearchResponse>(`${this.apiUrl}/search`, {
      params: httpParams,
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).pipe(
      tap({
        next: (response) => {
          this.logger.info(`OrderService.searchOrders() - Response received:`, response);
          this.logger.info(`OrderService.searchOrders() - Found ${response.totalElements} orders, content length: ${response.content?.length || 0}`);
        },
        error: (error) => {
          this.logger.error('OrderService.searchOrders() - Error:', {
            error: error,
            status: error?.status,
            statusText: error?.statusText,
            message: error?.message,
            errorBody: error?.error
          });
        }
      })
    );
  }
}


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { StockItem, MenuItemIngredient, StockWarning } from '../models/stock.models';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = `${environment.apiUrl}/stock`;

  constructor(private http: HttpClient) {}

  getStockItems(): Observable<StockItem[]> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<StockItem[]>(`${this.apiUrl}/items`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  getActiveStockItems(): Observable<StockItem[]> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<StockItem[]>(`${this.apiUrl}/items/active`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  getStockItemById(id: number): Observable<StockItem> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<StockItem>(`${this.apiUrl}/items/${id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  createStockItem(stockItem: StockItem): Observable<StockItem> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<StockItem>(`${this.apiUrl}/items`, stockItem, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  updateStockItem(id: number, stockItem: StockItem): Observable<StockItem> {
    const token = localStorage.getItem('accessToken');
    return this.http.put<StockItem>(`${this.apiUrl}/items/${id}`, stockItem, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  deleteStockItem(id: number): Observable<void> {
    const token = localStorage.getItem('accessToken');
    return this.http.delete<void>(`${this.apiUrl}/items/${id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  getMenuItemIngredients(menuItemId: number): Observable<MenuItemIngredient[]> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<MenuItemIngredient[]>(`${this.apiUrl}/menu-items/${menuItemId}/ingredients`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  saveMenuItemIngredients(menuItemId: number, ingredients: MenuItemIngredient[]): Observable<void> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<void>(`${this.apiUrl}/menu-items/${menuItemId}/ingredients`, ingredients, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  adjustStock(id: number, quantity: number, notes?: string): Observable<void> {
    const token = localStorage.getItem('accessToken');
    const params: any = { quantity };
    if (notes) params.notes = notes;
    return this.http.post<void>(`${this.apiUrl}/items/${id}/adjust`, null, {
      params,
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  getStockWarnings(): Observable<StockWarning[]> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<StockWarning[]>(`${this.apiUrl}/warnings`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  acknowledgeWarning(id: number): Observable<void> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<void>(`${this.apiUrl}/warnings/${id}/acknowledge`, null, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  acknowledgeAllWarnings(): Observable<void> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<void>(`${this.apiUrl}/warnings/acknowledge-all`, null, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  getWarningInterval(): Observable<{ intervalHours: number }> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<{ intervalHours: number }>(`${this.apiUrl}/warnings/config/interval`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  setWarningInterval(hours: number): Observable<void> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<void>(`${this.apiUrl}/warnings/config/interval`, null, {
      params: { hours: hours.toString() },
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  checkWarningsNow(): Observable<StockWarning[]> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<StockWarning[]>(`${this.apiUrl}/warnings/check-now`, null, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  getAlertsEnabled(): Observable<{ alertsEnabled: boolean }> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<{ alertsEnabled: boolean }>(`${this.apiUrl}/warnings/config/alerts-enabled`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  setAlertsEnabled(enabled: boolean): Observable<void> {
    const token = localStorage.getItem('accessToken');
    return this.http.post<void>(`${this.apiUrl}/warnings/config/alerts-enabled`, null, {
      params: { enabled: enabled.toString() },
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }
}


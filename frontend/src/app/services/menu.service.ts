import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MenuCategory {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  displayOrder?: number;
  imageUrl?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuItemSize {
  id?: number;
  sizeCode: string;
  sizeNameEn: string;
  sizeNameUr?: string;
  priceModifier?: number;
  isAvailable?: boolean;
  displayOrder?: number;
}

export interface AddOn {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  price?: number;
  isAvailable?: boolean;
  displayOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuItem {
  id?: number;
  categoryId: number;
  categoryName?: string;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  basePrice: number;
  imageUrl?: string;
  isAvailable?: boolean;
  isCombo?: boolean;
  displayOrder?: number;
  sizes?: MenuItemSize[];
  availableAddOns?: AddOn[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ComboItem {
  id?: number;
  menuItemId: number;
  menuItemName?: string;
  quantity: number;
  displayOrder?: number;
}

export interface Combo {
  id?: number;
  nameEn: string;
  nameUr?: string;
  descriptionEn?: string;
  descriptionUr?: string;
  comboPrice: number;
  imageUrl?: string;
  isAvailable?: boolean;
  displayOrder?: number;
  items?: ComboItem[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private apiUrl = `${environment.apiUrl}/menu`;

  constructor(private http: HttpClient) {}

  // Categories
  getCategories(active?: boolean): Observable<MenuCategory[]> {
    let params = new HttpParams();
    if (active !== undefined) {
      params = params.set('active', active.toString());
    }
    return this.http.get<MenuCategory[]>(`${this.apiUrl}/categories`, { params });
  }

  getCategory(id: number): Observable<MenuCategory> {
    return this.http.get<MenuCategory>(`${this.apiUrl}/categories/${id}`);
  }

  createCategory(category: MenuCategory): Observable<MenuCategory> {
    return this.http.post<MenuCategory>(`${this.apiUrl}/categories`, category);
  }

  updateCategory(id: number, category: MenuCategory): Observable<MenuCategory> {
    return this.http.put<MenuCategory>(`${this.apiUrl}/categories/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }

  // Menu Items
  getMenuItems(categoryId?: number): Observable<MenuItem[]> {
    let params = new HttpParams();
    if (categoryId !== undefined) {
      params = params.set('categoryId', categoryId.toString());
    }
    return this.http.get<MenuItem[]>(`${this.apiUrl}/items`, { params });
  }

  getMenuItem(id: number): Observable<MenuItem> {
    return this.http.get<MenuItem>(`${this.apiUrl}/items/${id}`);
  }

  createMenuItem(item: MenuItem): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.apiUrl}/items`, item);
  }

  updateMenuItem(id: number, item: MenuItem): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${this.apiUrl}/items/${id}`, item);
  }

  deleteMenuItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/items/${id}`);
  }

  // Add-ons
  getAddOns(available?: boolean): Observable<AddOn[]> {
    let params = new HttpParams();
    if (available !== undefined) {
      params = params.set('available', available.toString());
    }
    return this.http.get<AddOn[]>(`${this.apiUrl}/add-ons`, { params });
  }

  getAddOn(id: number): Observable<AddOn> {
    return this.http.get<AddOn>(`${this.apiUrl}/add-ons/${id}`);
  }

  createAddOn(addOn: AddOn): Observable<AddOn> {
    return this.http.post<AddOn>(`${this.apiUrl}/add-ons`, addOn);
  }

  updateAddOn(id: number, addOn: AddOn): Observable<AddOn> {
    return this.http.put<AddOn>(`${this.apiUrl}/add-ons/${id}`, addOn);
  }

  deleteAddOn(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/add-ons/${id}`);
  }

  // Combos
  getCombos(available?: boolean): Observable<Combo[]> {
    let params = new HttpParams();
    if (available !== undefined) {
      params = params.set('available', available.toString());
    }
    return this.http.get<Combo[]>(`${this.apiUrl}/combos`, { params });
  }

  getCombo(id: number): Observable<Combo> {
    return this.http.get<Combo>(`${this.apiUrl}/combos/${id}`);
  }

  createCombo(combo: Combo): Observable<Combo> {
    return this.http.post<Combo>(`${this.apiUrl}/combos`, combo);
  }

  updateCombo(id: number, combo: Combo): Observable<Combo> {
    return this.http.put<Combo>(`${this.apiUrl}/combos/${id}`, combo);
  }

  deleteCombo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/combos/${id}`);
  }
}


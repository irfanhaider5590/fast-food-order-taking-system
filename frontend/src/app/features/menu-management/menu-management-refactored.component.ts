import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryListComponent } from './components/category-list/category-list.component';
import { CategoryFormComponent } from './components/category-form/category-form.component';
import { MenuCategory } from '../../models/menu.models';

/**
 * Refactored Menu Management Component
 * Uses tabs and smaller focused components for better maintainability
 */
@Component({
  selector: 'app-menu-management-refactored',
  standalone: true,
  imports: [
    CommonModule,
    CategoryListComponent,
    CategoryFormComponent
  ],
  templateUrl: './menu-management-refactored.component.html',
  styleUrls: ['./menu-management-refactored.component.css']
})
export class MenuManagementRefactoredComponent implements OnInit {
  activeTab: 'categories' | 'items' | 'addons' | 'combos' = 'categories';
  
  // Category management
  showCategoryForm = false;
  editingCategory: MenuCategory | null = null;

  ngOnInit(): void {
    // Component initialization
  }

  setTab(tab: 'categories' | 'items' | 'addons' | 'combos'): void {
    this.activeTab = tab;
    this.closeForms();
  }

  // Category handlers
  onCategoryEdit(category: MenuCategory | null): void {
    this.editingCategory = category;
    this.showCategoryForm = true;
  }

  onCategorySaved(): void {
    this.showCategoryForm = false;
    this.editingCategory = null;
    // Refresh will be handled by category-list component
  }

  onCategoryCancelled(): void {
    this.showCategoryForm = false;
    this.editingCategory = null;
  }

  onCategoryRefresh(): void {
    // Category list will refresh itself
  }

  private closeForms(): void {
    this.showCategoryForm = false;
    this.editingCategory = null;
    // Add other form close handlers here
  }
}


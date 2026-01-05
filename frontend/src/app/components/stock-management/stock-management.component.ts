import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StockService } from '../../services/stock.service';
import { MenuService } from '../../services/menu.service';
import { StockItem, MenuItemIngredient } from '../../models/stock.models';
import { MenuItem } from '../../models/menu.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-stock-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './stock-management.component.html',
  styleUrls: ['./stock-management.component.css']
})
export class StockManagementComponent implements OnInit {
  activeTab: 'items' | 'ingredients' = 'items';
  
  // Stock Items
  stockItems: StockItem[] = [];
  stockItemForm: FormGroup;
  editingStockItem: StockItem | null = null;
  showStockItemForm = false;
  
  // Menu Item Ingredients
  menuItems: MenuItem[] = [];
  selectedMenuItem: MenuItem | null = null;
  menuItemIngredients: MenuItemIngredient[] = [];
  availableStockItems: StockItem[] = [];
  showIngredientForm = false;
  
  // Loading states
  loading = false;
  
  // Warning interval configuration
  warningIntervalHours = 2;
  showIntervalConfig = false;
  alertsEnabled = true;

  constructor(
    private stockService: StockService,
    private menuService: MenuService,
    private fb: FormBuilder,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    this.stockItemForm = this.fb.group({
      nameEn: ['', Validators.required],
      nameUr: [''],
      descriptionEn: [''],
      descriptionUr: [''],
      unit: ['piece', Validators.required],
      currentQuantity: [0, [Validators.required, Validators.min(0)]],
      minThreshold: [0, [Validators.required, Validators.min(0)]],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    // Use setTimeout to avoid ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      this.loadStockItems();
      this.loadMenuItems();
      this.loadWarningInterval();
      this.loadAlertsEnabled();
    }, 0);
  }

  loadWarningInterval(): void {
    this.stockService.getWarningInterval().subscribe({
      next: (config) => {
        this.warningIntervalHours = config.intervalHours;
      },
      error: (err) => {
        this.logger.error('Error loading warning interval:', err);
      }
    });
  }

  saveWarningInterval(): void {
    if (this.warningIntervalHours < 1 || this.warningIntervalHours > 24) {
      this.notificationService.showError('Warning interval must be between 1 and 24 hours', 3000);
      return;
    }

    this.stockService.setWarningInterval(this.warningIntervalHours).subscribe({
      next: () => {
        this.notificationService.showSuccess(`Warning interval set to ${this.warningIntervalHours} hours`, 3000);
        this.showIntervalConfig = false;
      },
      error: (err) => {
        this.logger.error('Error saving warning interval:', err);
        this.notificationService.showError('Failed to save warning interval', 3000);
      }
    });
  }

  checkWarningsNow(): void {
    this.stockService.checkWarningsNow().subscribe({
      next: (warnings) => {
        if (warnings.length > 0) {
          warnings.forEach(warning => {
            const message = `${warning.warningMessageEn}\n${warning.warningMessageUr}`;
            this.notificationService.showWarning(message, 8000);
          });
          this.notificationService.showInfo(`Found ${warnings.length} low stock warning(s)`, 3000);
        } else {
          this.notificationService.showSuccess('No low stock warnings found', 3000);
        }
      },
      error: (err) => {
        this.logger.error('Error checking warnings:', err);
        this.notificationService.showError('Failed to check warnings', 3000);
      }
    });
  }

  loadAlertsEnabled(): void {
    this.stockService.getAlertsEnabled().subscribe({
      next: (config) => {
        this.alertsEnabled = config.alertsEnabled;
      },
      error: (err) => {
        this.logger.error('Error loading alerts enabled status:', err);
      }
    });
  }

  toggleAlerts(): void {
    this.stockService.setAlertsEnabled(this.alertsEnabled).subscribe({
      next: () => {
        const status = this.alertsEnabled ? 'enabled' : 'disabled';
        this.notificationService.showSuccess(`Stock alerts ${status}`, 3000);
      },
      error: (err) => {
        this.logger.error('Error toggling alerts:', err);
        this.notificationService.showError('Failed to update alerts setting', 3000);
        // Revert the toggle on error
        this.alertsEnabled = !this.alertsEnabled;
      }
    });
  }

  setTab(tab: 'items' | 'ingredients'): void {
    this.activeTab = tab;
    if (tab === 'items') {
      this.loadStockItems();
    } else {
      this.loadMenuItems();
    }
  }

  // Stock Items Management
  loadStockItems(): void {
    this.loading = true;
    this.cdr.detectChanges(); // Trigger change detection for loading state
    this.stockService.getStockItems().subscribe({
      next: (data) => {
        this.stockItems = data;
        this.availableStockItems = data.filter(item => item.isActive);
        this.loading = false;
        this.cdr.detectChanges(); // Trigger change detection after data loaded
      },
      error: (err) => {
        this.logger.error('Error loading stock items:', err);
        this.notificationService.showError('Failed to load stock items', 3000);
        this.loading = false;
        this.cdr.detectChanges(); // Trigger change detection on error
      }
    });
  }

  openStockItemForm(item?: StockItem): void {
    this.showStockItemForm = true;
    this.editingStockItem = item || null;
    if (item) {
      this.stockItemForm.patchValue(item);
    } else {
      this.stockItemForm.reset({
        unit: 'piece',
        currentQuantity: 0,
        minThreshold: 0,
        isActive: true
      });
    }
  }

  saveStockItem(): void {
    if (this.stockItemForm.valid) {
      const stockItemData = this.stockItemForm.value;
      if (this.editingStockItem?.id) {
        this.stockService.updateStockItem(this.editingStockItem.id, stockItemData).subscribe({
          next: () => {
            this.notificationService.showSuccess('Stock item updated successfully', 3000);
            this.resetStockItemForm();
            this.loadStockItems();
          },
          error: (err) => {
            this.logger.error('Error updating stock item:', err);
            this.notificationService.showError('Failed to update stock item', 3000);
          }
        });
      } else {
        this.stockService.createStockItem(stockItemData).subscribe({
          next: () => {
            this.notificationService.showSuccess('Stock item created successfully', 3000);
            this.resetStockItemForm();
            this.loadStockItems();
          },
          error: (err) => {
            this.logger.error('Error creating stock item:', err);
            this.notificationService.showError('Failed to create stock item', 3000);
          }
        });
      }
    } else {
      this.notificationService.showError('Please fill in all required fields', 3000);
    }
  }

  deleteStockItem(id: number): void {
    if (confirm('Are you sure you want to delete this stock item?')) {
      this.stockService.deleteStockItem(id).subscribe({
        next: () => {
          this.notificationService.showSuccess('Stock item deleted successfully', 3000);
          this.loadStockItems();
        },
        error: (err) => {
          this.logger.error('Error deleting stock item:', err);
          this.notificationService.showError('Failed to delete stock item', 3000);
        }
      });
    }
  }

  adjustStock(item: StockItem, adjustment: number): void {
    const newQuantity = item.currentQuantity + adjustment;
    if (newQuantity < 0) {
      this.notificationService.showError('Cannot adjust stock below zero', 3000);
      return;
    }
    
    this.stockService.adjustStock(item.id!, adjustment, `Manual adjustment: ${adjustment > 0 ? '+' : ''}${adjustment}`).subscribe({
      next: () => {
        this.notificationService.showSuccess(`Stock adjusted by ${adjustment > 0 ? '+' : ''}${adjustment}`, 3000);
        this.loadStockItems();
      },
      error: (err) => {
        this.logger.error('Error adjusting stock:', err);
        this.notificationService.showError('Failed to adjust stock', 3000);
      }
    });
  }

  resetStockItemForm(): void {
    this.editingStockItem = null;
    this.showStockItemForm = false;
    this.stockItemForm.reset({
      unit: 'piece',
      currentQuantity: 0,
      minThreshold: 0,
      isActive: true
    });
  }

  // Menu Item Ingredients Management
  loadMenuItems(): void {
    this.menuService.getMenuItems().subscribe({
      next: (data) => {
        this.menuItems = data.filter(item => !item.isCombo);
      },
      error: (err) => {
        this.logger.error('Error loading menu items:', err);
      }
    });
  }

  selectMenuItem(menuItem: MenuItem): void {
    this.selectedMenuItem = menuItem;
    this.loadMenuItemIngredients(menuItem.id!);
  }

  loadMenuItemIngredients(menuItemId: number): void {
    this.stockService.getMenuItemIngredients(menuItemId).subscribe({
      next: (data) => {
        this.menuItemIngredients = data;
      },
      error: (err) => {
        this.logger.error('Error loading menu item ingredients:', err);
        this.menuItemIngredients = [];
      }
    });
  }

  addIngredient(): void {
    this.menuItemIngredients.push({
      stockItemId: 0,
      quantityRequired: 1
    });
  }

  removeIngredient(index: number): void {
    this.menuItemIngredients.splice(index, 1);
  }

  saveMenuItemIngredients(): void {
    if (!this.selectedMenuItem?.id) {
      this.notificationService.showError('Please select a menu item first', 3000);
      return;
    }

    const validIngredients = this.menuItemIngredients.filter(ing => ing.stockItemId > 0);
    if (validIngredients.length === 0) {
      this.notificationService.showError('Please add at least one ingredient', 3000);
      return;
    }

    // Check for duplicates in the list
    const stockItemIds = validIngredients.map(ing => ing.stockItemId);
    const uniqueIds = new Set(stockItemIds);
    if (stockItemIds.length !== uniqueIds.size) {
      this.notificationService.showError('Duplicate ingredients found. Please remove duplicates before saving.', 5000);
      return;
    }

    this.stockService.saveMenuItemIngredients(this.selectedMenuItem.id, validIngredients).subscribe({
      next: () => {
        this.notificationService.showSuccess('Ingredients saved successfully', 3000);
        this.loadMenuItemIngredients(this.selectedMenuItem!.id!);
      },
      error: (err) => {
        this.logger.error('Error saving ingredients:', err);
        const errorMessage = err.error?.message || 'Failed to save ingredients';
        this.notificationService.showError(errorMessage, 5000);
      }
    });
  }

  getStockItemName(stockItemId: number): string {
    const item = this.availableStockItems.find(s => s.id === stockItemId);
    return item?.nameEn || 'Select stock item';
  }
}


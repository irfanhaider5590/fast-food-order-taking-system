import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StockService } from '../../services/stock.service';
import { StockAlertService } from '../../services/stock-alert.service';
import {
  StockItem,
  StockConsumptionRow,
  StockConsumptionConfig,
  ConsumptionCatalog,
  ConsumptionCatalogCategory,
  ConsumptionCatalogItem
} from '../../models/stock.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';

/** Editable draft row for Consumption Config tab */
export interface ConsumptionDraftRow {
  categoryId: number | null;
  menuItemId: number | null;
  sizeCode: string | null;
  servingsPerUnit: number;
}

@Component({
  selector: 'app-stock-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './stock-management.component.html',
  styleUrls: ['./stock-management.component.css']
})
export class StockManagementComponent implements OnInit {
  activeTab: 'stock' | 'consumptions' = 'stock';
  isAdmin = false;

  // Stock list
  stockItems: StockItem[] = [];
  availableStockItems: StockItem[] = [];
  loading = false;

  // Quantity — add or remove (expired / damaged)
  selectedQuantityStockId: number | null = null;
  quantityValue: number | null = null;
  quantityMode: 'add' | 'remove' | 'edit' = 'add';
  removeReason = 'Expired / damaged';
  savingQuantity = false;
  barcodeBuffer = '';
  lastScanMessage = '';
  stockSearchQuery = '';
  private scanning = false;

  // Admin: edit / create stock name (same panel as Add / Remove)
  catalogForm: FormGroup;
  editingStockItem: StockItem | null = null;

  // Consumption Config (admin)
  selectedConsumptionStock: StockItem | null = null;
  consumptionConfig: StockConsumptionConfig | null = null;
  consumptionCatalog: ConsumptionCatalog | null = null;
  consumptionRows: ConsumptionDraftRow[] = [];
  savingConsumptions = false;

  // Warning settings (admin)
  warningIntervalHours = 2;
  showIntervalConfig = false;
  alertsEnabled = true;

  constructor(
    private stockService: StockService,
    private stockAlertService: StockAlertService,
    private fb: FormBuilder,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    this.catalogForm = this.fb.group({
      nameEn: ['', Validators.required],
      nameUr: [''],
      unit: ['piece', Validators.required],
      minThreshold: [0, [Validators.required, Validators.min(0)]],
      barcode: [''],
      scanPackQty: [1, [Validators.required, Validators.min(0.0001)]],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    this.resolveAdmin();
    setTimeout(() => {
      this.loadStockItems();
      if (this.isAdmin) {
        this.loadWarningInterval();
        this.loadAlertsEnabled();
      }
    }, 0);
  }

  private resolveAdmin(): void {
    try {
      const userStr = localStorage.getItem('user');
      if (!userStr) {
        this.isAdmin = false;
        return;
      }
      const user = JSON.parse(userStr);
      const role = user?.role || user?.roleName;
      const roleId = user?.roleId;
      this.isAdmin = role === 'ADMIN' ||
        role === 'Admin' ||
        role?.toLowerCase() === 'admin' ||
        roleId === 1;
    } catch {
      this.isAdmin = false;
    }
  }

  setTab(tab: 'stock' | 'consumptions'): void {
    if (!this.isAdmin && tab !== 'stock') {
      return;
    }
    this.activeTab = tab;
    this.loadStockItems();
    if (tab === 'stock') {
      this.focusBarcode();
    } else {
      this.loadConsumptionCatalog();
    }
  }

  loadStockItems(): void {
    this.loading = true;
    this.cdr.detectChanges();
    this.stockService.getStockItems().subscribe({
      next: (data) => {
        this.stockItems = data;
        this.availableStockItems = data.filter(item => item.isActive !== false);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Error loading stock items:', err);
        this.notificationService.showError('Failed to load stock items', 3000);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get selectedQuantityStock(): StockItem | null {
    if (this.selectedQuantityStockId == null) {
      return null;
    }
    return this.availableStockItems.find(s => s.id === this.selectedQuantityStockId)
      || this.stockItems.find(s => s.id === this.selectedQuantityStockId)
      || null;
  }

  /** Partial name / barcode search (admin sees inactive items too) */
  get filteredStockItems(): StockItem[] {
    const source = this.isAdmin ? this.stockItems : this.availableStockItems;
    return this.filterStockItems(source, this.stockSearchQuery);
  }

  /** Active items for quantity dropdown / barcode restock */
  get filteredAvailableStockItems(): StockItem[] {
    return this.filterStockItems(this.availableStockItems, this.stockSearchQuery);
  }

  private filterStockItems(items: StockItem[], query: string): StockItem[] {
    const q = (query || '').trim().toLowerCase();
    if (!q) {
      return items;
    }
    return items.filter(item => {
      const nameEn = (item.nameEn || '').toLowerCase();
      const nameUr = (item.nameUr || '').toLowerCase();
      const barcode = (item.barcode || '').toLowerCase();
      return nameEn.includes(q) || nameUr.includes(q) || barcode.includes(q);
    });
  }

  onQuantityStockSelected(): void {
    this.quantityValue = null;
    this.lastScanMessage = '';
    if (this.quantityMode === 'edit' && this.isAdmin) {
      this.patchCatalogForm(this.selectedQuantityStock);
    }
  }

  setQuantityMode(mode: 'add' | 'remove' | 'edit'): void {
    if (mode === 'edit' && !this.isAdmin) {
      return;
    }
    this.quantityMode = mode;
    this.quantityValue = null;
    this.lastScanMessage = '';
    if (mode === 'edit') {
      const stock = this.selectedQuantityStock;
      if (stock) {
        this.patchCatalogForm(stock);
      } else {
        this.patchCatalogForm(null);
      }
      this.scrollToQuantityForm();
    } else if (mode === 'add') {
      this.editingStockItem = null;
      this.focusBarcode();
    } else {
      this.editingStockItem = null;
    }
  }

  selectStockForQuantity(item: StockItem, mode?: 'add' | 'remove' | 'edit'): void {
    if (mode === 'edit' && !this.isAdmin) {
      return;
    }
    if (mode) {
      this.quantityMode = mode;
    }
    this.selectedQuantityStockId = item.id ?? null;
    this.quantityValue = null;
    this.lastScanMessage = '';
    if (this.quantityMode === 'edit') {
      this.patchCatalogForm(item);
    }
    this.scrollToQuantityForm();
    if (this.quantityMode === 'add') {
      this.focusBarcode();
    }
  }

  /** Header “+ Add Stock Name” — create new in Edit mode */
  startNewStockName(): void {
    if (!this.isAdmin) {
      return;
    }
    this.quantityMode = 'edit';
    this.selectedQuantityStockId = null;
    this.quantityValue = null;
    this.lastScanMessage = '';
    this.patchCatalogForm(null);
    this.scrollToQuantityForm();
  }

  private patchCatalogForm(item: StockItem | null): void {
    this.editingStockItem = item;
    if (item) {
      this.catalogForm.patchValue({
        nameEn: item.nameEn,
        nameUr: item.nameUr || '',
        unit: item.unit || 'piece',
        minThreshold: item.minThreshold,
        barcode: item.barcode || '',
        scanPackQty: item.scanPackQty != null ? item.scanPackQty : 1,
        isActive: item.isActive !== false
      });
    } else {
      this.catalogForm.reset({
        nameEn: '',
        nameUr: '',
        unit: 'piece',
        minThreshold: 0,
        barcode: '',
        scanPackQty: 1,
        isActive: true
      });
    }
  }

  private scrollToQuantityForm(): void {
    setTimeout(() => {
      document.querySelector('.quantity-card')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }, 0);
  }

  previewAfterSave(stock: StockItem): number {
    const delta = Number(this.quantityValue || 0);
    const signed = this.quantityMode === 'remove' ? -delta : delta;
    return Math.round((Number(stock.currentQuantity) + signed) * 10000) / 10000;
  }

  focusBarcode(): void {
    setTimeout(() => {
      const el = document.querySelector('.barcode-input') as HTMLInputElement | null;
      el?.focus();
    }, 0);
  }

  onBarcodeEnter(event: Event): void {
    event.preventDefault();
    const raw = (this.barcodeBuffer || '').trim();
    this.barcodeBuffer = '';
    if (!raw || this.scanning) {
      return;
    }
    this.scanning = true;
    this.stockService.lookupByBarcode(raw).subscribe({
      next: (item) => {
        this.scanning = false;
        if (!item?.id) {
          this.lastScanMessage = `Unknown barcode: ${raw}`;
          this.notificationService.showError(this.lastScanMessage, 3000);
          this.focusBarcode();
          return;
        }
        const encodedQty = this.parseQtyFromScan(raw);
        const pack = encodedQty != null
          ? encodedQty
          : Number(item.scanPackQty != null ? item.scanPackQty : 1);
        this.selectedQuantityStockId = item.id;
        // Refresh list entry with latest qty if present
        const existing = this.stockItems.find(s => s.id === item.id);
        if (existing) {
          Object.assign(existing, item);
        }
        this.quantityValue = Math.round(((this.quantityValue || 0) + pack) * 10000) / 10000;
        const verb = this.quantityMode === 'remove' ? 'to remove' : 'to add';
        this.lastScanMessage = `Scanned ${item.nameEn}: ${pack} ${item.unit || ''} (${verb}: ${this.quantityValue})`;
        this.notificationService.showSuccess(this.lastScanMessage, 2500);
        this.cdr.detectChanges();
        this.focusBarcode();
      },
      error: (err) => {
        this.scanning = false;
        this.lastScanMessage = err.error?.message || `No stock item for barcode: ${raw}`;
        this.notificationService.showError(this.lastScanMessage, 4000);
        this.focusBarcode();
        this.cdr.detectChanges();
      }
    });
  }

  /** Optional CODE:QTY or CODE*QTY from labeled barcodes */
  private parseQtyFromScan(raw: string): number | null {
    const code = (raw || '').trim();
    const colon = code.lastIndexOf(':');
    const star = code.lastIndexOf('*');
    const sep = Math.max(colon, star);
    if (sep > 0 && sep < code.length - 1) {
      const n = Number(code.substring(sep + 1));
      if (!Number.isNaN(n) && n > 0) {
        return n;
      }
    }
    return null;
  }

  saveQuantity(): void {
    const stock = this.selectedQuantityStock;
    if (!stock?.id) {
      this.notificationService.showError('Please select a stock item', 3000);
      return;
    }
    if (this.quantityValue == null || this.quantityValue <= 0) {
      this.notificationService.showError(
        this.quantityMode === 'remove'
          ? 'Enter a quantity to remove (greater than 0)'
          : 'Enter a quantity to add (greater than 0)',
        3000
      );
      return;
    }

    const qty = this.quantityValue;
    const isRemove = this.quantityMode === 'remove';
    if (isRemove && qty > Number(stock.currentQuantity)) {
      this.notificationService.showError(
        `Cannot remove ${qty}: only ${stock.currentQuantity} ${stock.unit} on hand`,
        4000
      );
      return;
    }

    const signedQty = isRemove ? -qty : qty;
    const reason = isRemove
      ? (this.removeReason || 'Expired / damaged').trim()
      : `Restock +${qty}`;
    const notes = isRemove ? `Remove ${qty}: ${reason}` : reason;

    this.savingQuantity = true;
    this.stockService.adjustStock(stock.id, signedQty, notes).subscribe({
      next: () => {
        this.savingQuantity = false;
        this.notificationService.showSuccess(
          isRemove
            ? `Removed ${qty} ${stock.unit} from ${stock.nameEn}`
            : `Added ${qty} ${stock.unit} to ${stock.nameEn}`,
          3000
        );
        this.quantityValue = null;
        this.lastScanMessage = '';
        this.loadStockItems();
        this.stockAlertService.refreshLiveWarnings().subscribe();
        this.cdr.detectChanges();
        this.focusBarcode();
      },
      error: (err) => {
        this.savingQuantity = false;
        this.logger.error('Error adjusting stock:', err);
        this.notificationService.showError(err.error?.message || 'Failed to update stock', 4000);
        this.cdr.detectChanges();
      }
    });
  }

  // --- Admin catalog (Edit mode in same panel) ---
  resetCatalogForm(): void {
    this.editingStockItem = null;
    this.selectedQuantityStockId = null;
    this.catalogForm.reset({
      nameEn: '',
      nameUr: '',
      unit: 'piece',
      minThreshold: 0,
      barcode: '',
      scanPackQty: 1,
      isActive: true
    });
    this.quantityMode = 'add';
  }

  saveCatalogItem(): void {
    if (!this.isAdmin) {
      return;
    }
    if (!this.catalogForm.valid) {
      this.notificationService.showError('Please fill in name, unit and threshold', 3000);
      return;
    }

    const form = this.catalogForm.value;
    const payload: StockItem = {
      nameEn: form.nameEn,
      nameUr: form.nameUr,
      unit: form.unit,
      minThreshold: form.minThreshold,
      isActive: form.isActive,
      barcode: form.barcode || null,
      scanPackQty: form.scanPackQty,
      currentQuantity: this.editingStockItem?.currentQuantity ?? 0
    };

    if (this.editingStockItem?.id) {
      this.stockService.updateStockItem(this.editingStockItem.id, payload).subscribe({
        next: () => {
          this.notificationService.showSuccess('Stock name updated', 3000);
          this.resetCatalogForm();
          this.loadStockItems();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.logger.error('Error updating catalog item:', err);
          this.notificationService.showError(err.error?.message || 'Failed to update stock name', 4000);
        }
      });
    } else {
      this.stockService.createStockItem(payload).subscribe({
        next: () => {
          this.notificationService.showSuccess('Stock name added', 3000);
          this.resetCatalogForm();
          this.loadStockItems();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.logger.error('Error creating catalog item:', err);
          this.notificationService.showError(err.error?.message || 'Failed to add stock name', 4000);
        }
      });
    }
  }

  deleteStockItem(id: number): void {
    if (!this.isAdmin) {
      return;
    }
    if (!confirm('Delete this stock name? This cannot be undone.')) {
      return;
    }
    this.stockService.deleteStockItem(id).subscribe({
      next: () => {
        this.notificationService.showSuccess('Stock name deleted', 3000);
        if (this.selectedQuantityStockId === id) {
          this.selectedQuantityStockId = null;
          this.quantityValue = null;
        }
        this.loadStockItems();
      },
      error: (err) => {
        this.logger.error('Error deleting stock item:', err);
        this.notificationService.showError(err.error?.message || 'Failed to delete stock name', 4000);
      }
    });
  }

  // --- Warning settings (admin) ---
  loadWarningInterval(): void {
    this.stockService.getWarningInterval().subscribe({
      next: (config) => {
        this.warningIntervalHours = config.intervalHours;
      },
      error: (err) => this.logger.error('Error loading warning interval:', err)
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
            this.notificationService.showWarning(
              `${warning.warningMessageEn}\n${warning.warningMessageUr}`,
              8000
            );
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
      error: (err) => this.logger.error('Error loading alerts enabled status:', err)
    });
  }

  toggleAlerts(): void {
    this.stockService.setAlertsEnabled(this.alertsEnabled).subscribe({
      next: () => {
        this.notificationService.showSuccess(
          `Stock alerts ${this.alertsEnabled ? 'enabled' : 'disabled'}`,
          3000
        );
      },
      error: (err) => {
        this.logger.error('Error toggling alerts:', err);
        this.notificationService.showError('Failed to update alerts setting', 3000);
        this.alertsEnabled = !this.alertsEnabled;
      }
    });
  }

  // --- Consumption config (admin) ---
  loadConsumptionCatalog(): void {
    this.stockService.getConsumptionCatalog().subscribe({
      next: (catalog) => {
        this.consumptionCatalog = catalog;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Error loading consumption catalog:', err);
        this.notificationService.showError('Failed to load menu catalog', 3000);
      }
    });
  }

  selectConsumptionStock(stock: StockItem | null): void {
    this.selectedConsumptionStock = stock;
    this.consumptionConfig = null;
    this.consumptionRows = [];
    if (!stock?.id) {
      return;
    }
    this.stockService.getStockConsumptions(stock.id).subscribe({
      next: (config) => {
        this.consumptionConfig = config;
        this.consumptionRows = (config.rows || []).map(r => ({
          categoryId: r.categoryId ?? null,
          menuItemId: r.menuItemId,
          sizeCode: r.sizeCode ?? null,
          servingsPerUnit: Number(r.servingsPerUnit)
        }));
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Error loading consumptions:', err);
        this.notificationService.showError('Failed to load consumption config', 3000);
      }
    });
  }

  addConsumptionRow(): void {
    this.consumptionRows.push({
      categoryId: null,
      menuItemId: null,
      sizeCode: null,
      servingsPerUnit: 10
    });
  }

  removeConsumptionRow(index: number): void {
    this.consumptionRows.splice(index, 1);
  }

  getCatalogCategories(): ConsumptionCatalogCategory[] {
    return this.consumptionCatalog?.categories || [];
  }

  getCatalogItemsForCategory(categoryId: number | null): ConsumptionCatalogItem[] {
    if (!categoryId || !this.consumptionCatalog) {
      return this.getCatalogCategories().flatMap(c => c.items);
    }
    return this.getCatalogCategories().find(c => c.id === categoryId)?.items || [];
  }

  getSizesForMenuItem(menuItemId: number | null): { sizeCode: string; sizeNameEn: string }[] {
    if (!menuItemId) {
      return [];
    }
    for (const cat of this.getCatalogCategories()) {
      const item = cat.items.find(i => i.id === menuItemId);
      if (item) {
        return item.sizes || [];
      }
    }
    return [];
  }

  onConsumptionCategoryChange(row: ConsumptionDraftRow): void {
    row.menuItemId = null;
    row.sizeCode = null;
  }

  onConsumptionMenuItemChange(row: ConsumptionDraftRow): void {
    row.sizeCode = null;
    if (row.menuItemId) {
      for (const cat of this.getCatalogCategories()) {
        const item = cat.items.find(i => i.id === row.menuItemId);
        if (item) {
          row.categoryId = item.categoryId;
          break;
        }
      }
    }
  }

  quantityPerServingPreview(servings: number): number {
    if (!servings || servings <= 0) {
      return 0;
    }
    return Math.round((1 / servings) * 1_000_000) / 1_000_000;
  }

  getMenuItemName(menuItemId: number | null): string {
    if (!menuItemId) {
      return 'product';
    }
    for (const cat of this.getCatalogCategories()) {
      const item = cat.items.find(i => i.id === menuItemId);
      if (item) {
        return item.nameEn;
      }
    }
    return 'product';
  }

  saveConsumptionConfig(): void {
    if (!this.selectedConsumptionStock?.id) {
      this.notificationService.showError('Select a stock item first', 3000);
      return;
    }

    const valid: StockConsumptionRow[] = [];
    for (const row of this.consumptionRows) {
      if (!row.menuItemId || !row.servingsPerUnit || row.servingsPerUnit <= 0) {
        this.notificationService.showError('Each row needs a menu item and servings > 0', 4000);
        return;
      }
      valid.push({
        menuItemId: row.menuItemId,
        sizeCode: row.sizeCode || null,
        servingsPerUnit: row.servingsPerUnit
      });
    }

    const keys = valid.map(r => `${r.menuItemId}|${r.sizeCode || ''}`);
    if (new Set(keys).size !== keys.length) {
      this.notificationService.showError('Duplicate menu item / size rows are not allowed', 4000);
      return;
    }

    this.savingConsumptions = true;
    this.stockService.saveStockConsumptions(this.selectedConsumptionStock.id, valid).subscribe({
      next: (config) => {
        this.consumptionConfig = config;
        this.consumptionRows = (config.rows || []).map(r => ({
          categoryId: r.categoryId ?? null,
          menuItemId: r.menuItemId,
          sizeCode: r.sizeCode ?? null,
          servingsPerUnit: Number(r.servingsPerUnit)
        }));
        this.savingConsumptions = false;
        this.notificationService.showSuccess('Consumption config saved', 3000);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.savingConsumptions = false;
        this.logger.error('Error saving consumptions:', err);
        this.notificationService.showError(err.error?.message || 'Failed to save consumption config', 5000);
        this.cdr.detectChanges();
      }
    });
  }
}

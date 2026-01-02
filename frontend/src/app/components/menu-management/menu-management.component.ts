import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MenuService } from '../../services/menu.service';
import { MenuCategory, MenuItem, AddOn, Combo, MenuItemSize, ComboItem } from '../../models/menu.models';
import { ImageUploadService } from '../../services/image-upload.service';
import { LoggerService } from '../../services/logger.service';

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.css']
})
export class MenuManagementComponent implements OnInit {
  activeTab: 'categories' | 'items' | 'addons' | 'combos' = 'categories';
  
  // Categories
  categories: MenuCategory[] = [];
  categoryForm: FormGroup;
  editingCategory: MenuCategory | null = null;
  showCategoryForm = false;
  categoryImagePreview: string | null = null;
  categoryImageFile: File | null = null;
  
  // Menu Items
  menuItems: MenuItem[] = [];
  menuItemForm: FormGroup;
  editingMenuItem: MenuItem | null = null;
  showMenuItemForm = false;
  showSizeForm = false;
  showAddOnForm = false;
  currentSizes: MenuItemSize[] = [];
  selectedAddOnIds: number[] = [];
  menuItemImagePreview: string | null = null;
  menuItemImageFile: File | null = null;
  
  // Add-ons
  addOns: AddOn[] = [];
  addOnForm: FormGroup;
  editingAddOn: AddOn | null = null;
  showAddOnFormFlag = false;
  
  // Combos
  combos: Combo[] = [];
  comboForm: FormGroup;
  editingCombo: Combo | null = null;
  showComboForm = false;
  availableMenuItems: MenuItem[] = [];
  comboItems: Array<{ menuItemId: number; quantity: number }> = [];
  comboImagePreview: string | null = null;
  comboImageFile: File | null = null;

  // Loading states
  uploadingImage = false;

  constructor(
    private menuService: MenuService,
    private fb: FormBuilder,
    private imageUploadService: ImageUploadService,
    private logger: LoggerService
  ) {
    this.categoryForm = this.fb.group({
      nameEn: ['', Validators.required],
      nameUr: [''],
      descriptionEn: [''],
      descriptionUr: [''],
      displayOrder: [0],
      imageUrl: [''],
      isActive: [true]
    });

    this.menuItemForm = this.fb.group({
      categoryId: ['', Validators.required],
      nameEn: ['', Validators.required],
      nameUr: [''],
      descriptionEn: [''],
      descriptionUr: [''],
      basePrice: ['', [Validators.required, Validators.min(0)]],
      imageUrl: [''],
      isAvailable: [true],
      isCombo: [false],
      displayOrder: [0]
    });

    this.addOnForm = this.fb.group({
      nameEn: ['', Validators.required],
      nameUr: [''],
      descriptionEn: [''],
      descriptionUr: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      isAvailable: [true],
      displayOrder: [0]
    });

    this.comboForm = this.fb.group({
      nameEn: ['', Validators.required],
      nameUr: [''],
      descriptionEn: [''],
      descriptionUr: [''],
      comboPrice: ['', [Validators.required, Validators.min(0)]],
      imageUrl: [''],
      isAvailable: [true],
      displayOrder: [0]
    });
  }

  ngOnInit(): void {
    this.loadAllData();
  }

  // Tab Management
  setTab(tab: 'categories' | 'items' | 'addons' | 'combos'): void {
    this.activeTab = tab;
    // Reload data when switching tabs to ensure fresh data
    this.loadAllData();
  }

  private loadAllData(): void {
    this.loadCategories();
    this.loadMenuItems();
    this.loadAddOns();
    this.loadCombos();
  }

  // Categories
  loadCategories(): void {
    this.menuService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.logger.debug(`Loaded ${data.length} categories`);
      },
      error: (err) => {
        this.logger.error('Error loading categories:', err);
        this.categories = [];
      }
    });
  }

  openCategoryForm(category?: MenuCategory): void {
    this.showCategoryForm = true;
    this.editingCategory = category || null;
    this.categoryImagePreview = null;
    this.categoryImageFile = null;
    if (category) {
      this.categoryForm.patchValue(category);
      if (category.imageUrl) {
        this.categoryImagePreview = category.imageUrl;
      }
    } else {
      this.categoryForm.reset({ isActive: true, displayOrder: 0 });
    }
  }

  onCategoryImageSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const validation = this.imageUploadService.validateFile(file);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }

    this.categoryImageFile = file;
    this.uploadingImage = true;

    this.imageUploadService.compressImage(file).then((base64) => {
      this.categoryImagePreview = base64;
      const imageKey = this.imageUploadService.saveImageToLocalStorage(base64, 'category');
      this.categoryForm.patchValue({ imageUrl: imageKey });
      this.uploadingImage = false;
    }).catch((error) => {
      this.logger.error('Error compressing image:', error);
      alert('Failed to process image. Please try again.');
      this.uploadingImage = false;
    });
  }

  removeCategoryImage(): void {
    const imageUrl = this.categoryForm.get('imageUrl')?.value;
    if (imageUrl && imageUrl.startsWith('image_')) {
      this.imageUploadService.deleteImageFromLocalStorage(imageUrl);
    }
    this.categoryImagePreview = null;
    this.categoryImageFile = null;
    this.categoryForm.patchValue({ imageUrl: '' });
  }

  saveCategory(): void {
    if (this.categoryForm.valid) {
      const categoryData = { ...this.categoryForm.value };
      
      // Convert image key to base64 if it's stored locally
      if (categoryData.imageUrl && categoryData.imageUrl.startsWith('image_')) {
        const base64Image = this.imageUploadService.getImageFromLocalStorage(categoryData.imageUrl);
        if (base64Image) {
          categoryData.imageUrl = base64Image;
        }
      }

      if (this.editingCategory?.id) {
        this.menuService.updateCategory(this.editingCategory.id, categoryData).subscribe({
          next: () => {
            this.logger.info('Category updated successfully');
            this.resetCategoryForm();
            this.loadCategories();
          },
          error: (err) => {
            this.logger.error('Error updating category:', err);
            alert('Failed to update category: ' + (err.error?.message || 'Unknown error'));
          }
        });
      } else {
        this.menuService.createCategory(categoryData).subscribe({
          next: () => {
            this.logger.info('Category created successfully');
            this.resetCategoryForm();
            this.loadCategories();
          },
          error: (err) => {
            this.logger.error('Error creating category:', err);
            alert('Failed to create category: ' + (err.error?.message || 'Unknown error'));
          }
        });
      }
    } else {
      alert('Please fill in all required fields');
    }
  }

  deleteCategory(id: number): void {
    if (confirm('Are you sure you want to delete this category?')) {
      this.menuService.deleteCategory(id).subscribe({
        next: () => {
          this.logger.info('Category deleted successfully');
          this.loadCategories();
        },
        error: (err) => {
          this.logger.error('Error deleting category:', err);
          alert('Failed to delete category: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  private resetCategoryForm(): void {
    this.editingCategory = null;
    this.showCategoryForm = false;
    this.categoryImagePreview = null;
    this.categoryImageFile = null;
    this.categoryForm.reset({ isActive: true, displayOrder: 0 });
  }

  // Menu Items
  loadMenuItems(): void {
    this.menuService.getMenuItems().subscribe({
      next: (data) => {
        this.menuItems = data;
        this.availableMenuItems = data.filter(item => !item.isCombo);
        this.logger.debug(`Loaded ${data.length} menu items`);
        // Log sizes for debugging
        data.forEach(item => {
          if (item.sizes && item.sizes.length > 0) {
            this.logger.debug(`Item ${item.id} (${item.nameEn}) has ${item.sizes.length} sizes:`, JSON.stringify(item.sizes));
          }
        });
      },
      error: (err) => this.logger.error('Error loading menu items:', err)
    });
  }

  openMenuItemForm(item?: MenuItem): void {
    this.showMenuItemForm = true;
    this.editingMenuItem = item || null;
    
    // Load sizes - ensure we have a proper array
    if (item?.sizes && Array.isArray(item.sizes) && item.sizes.length > 0) {
      this.currentSizes = item.sizes.map((size: MenuItemSize) => ({
        id: size.id,
        sizeCode: size.sizeCode || '',
        sizeNameEn: size.sizeNameEn || '',
        sizeNameUr: size.sizeNameUr || '',
        priceModifier: size.priceModifier || 0,
        isAvailable: size.isAvailable !== undefined ? size.isAvailable : true,
        displayOrder: size.displayOrder || 0
      }));
      this.logger.info(`Loaded ${this.currentSizes.length} sizes for item ${item.id}:`, JSON.stringify(this.currentSizes));
    } else {
      this.currentSizes = [];
      this.logger.debug(`No sizes found for item ${item?.id}. Item sizes:`, item?.sizes);
    }
    
    this.selectedAddOnIds = item?.availableAddOns?.map((a: AddOn) => a.id!) || [];
    this.menuItemImagePreview = null;
    this.menuItemImageFile = null;
    
    this.logger.debug(`Opening menu item form. Item ID: ${item?.id}, Sizes loaded: ${this.currentSizes.length}`);
    
    if (item) {
      this.menuItemForm.patchValue({
        categoryId: item.categoryId,
        nameEn: item.nameEn,
        nameUr: item.nameUr,
        descriptionEn: item.descriptionEn,
        descriptionUr: item.descriptionUr,
        basePrice: item.basePrice,
        imageUrl: item.imageUrl,
        isAvailable: item.isAvailable ?? true,
        isCombo: item.isCombo ?? false,
        displayOrder: item.displayOrder ?? 0
      });
      if (item.imageUrl) {
        this.menuItemImagePreview = item.imageUrl;
      }
    } else {
      this.menuItemForm.reset({ isAvailable: true, isCombo: false, displayOrder: 0 });
      this.currentSizes = [];
      this.selectedAddOnIds = [];
    }
  }

  onMenuItemImageSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const validation = this.imageUploadService.validateFile(file);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }

    this.menuItemImageFile = file;
    this.uploadingImage = true;

    this.imageUploadService.compressImage(file).then((base64) => {
      this.menuItemImagePreview = base64;
      const imageKey = this.imageUploadService.saveImageToLocalStorage(base64, 'menuitem');
      this.menuItemForm.patchValue({ imageUrl: imageKey });
      this.uploadingImage = false;
    }).catch((error) => {
      this.logger.error('Error compressing image:', error);
      alert('Failed to process image. Please try again.');
      this.uploadingImage = false;
    });
  }

  removeMenuItemImage(): void {
    const imageUrl = this.menuItemForm.get('imageUrl')?.value;
    if (imageUrl && imageUrl.startsWith('image_')) {
      this.imageUploadService.deleteImageFromLocalStorage(imageUrl);
    }
    this.menuItemImagePreview = null;
    this.menuItemImageFile = null;
    this.menuItemForm.patchValue({ imageUrl: '' });
  }

  addSize(): void {
    const newSize = {
      sizeCode: '',
      sizeNameEn: '',
      priceModifier: 0,
      isAvailable: true,
      displayOrder: this.currentSizes.length
    };
    this.currentSizes.push(newSize);
    this.logger.debug(`Added new size. Total sizes now: ${this.currentSizes.length}`);
  }

  removeSize(index: number): void {
    this.currentSizes.splice(index, 1);
    this.currentSizes = [...this.currentSizes]; // Update reference for change detection
    this.logger.debug(`Removed size at index ${index}. Remaining sizes: ${this.currentSizes.length}`);
  }

  trackBySizeIndex(index: number, size: MenuItemSize): number {
    return index;
  }

  updateSizeField(index: number, field: string, value: any): void {
    if (!this.currentSizes[index]) {
      this.logger.error(`Cannot update size at index ${index} - index out of bounds. Array length: ${this.currentSizes.length}`);
      return;
    }
    
    // Update the existing object directly to preserve focus
    // Don't recreate the array - just update the property
    // Angular's change detection will pick up the property change
    if (field === 'priceModifier') {
      this.currentSizes[index].priceModifier = value ? parseFloat(value) : 0;
    } else if (field === 'sizeCode') {
      this.currentSizes[index].sizeCode = value || '';
    } else if (field === 'sizeNameEn') {
      this.currentSizes[index].sizeNameEn = value || '';
    } else if (field === 'sizeNameUr') {
      this.currentSizes[index].sizeNameUr = value || '';
    }
    
    // No need to recreate array - trackBy function handles DOM stability
  }

  toggleAddOn(addOnId: number): void {
    const index = this.selectedAddOnIds.indexOf(addOnId);
    if (index > -1) {
      this.selectedAddOnIds.splice(index, 1);
    } else {
      this.selectedAddOnIds.push(addOnId);
    }
  }

  saveMenuItem(): void {
    if (this.menuItemForm.valid) {
      // Log currentSizes before filtering
      this.logger.info(`Before filtering - currentSizes.length: ${this.currentSizes.length}`);
      this.logger.info(`currentSizes content:`, JSON.stringify(this.currentSizes, null, 2));
      
      // Filter out sizes with empty sizeCode or sizeNameEn
      const validSizes = this.currentSizes.filter(
        size => {
          const isValid = size.sizeCode && size.sizeCode.trim() !== '' && 
                         size.sizeNameEn && size.sizeNameEn.trim() !== '';
          if (!isValid && (size.sizeCode || size.sizeNameEn)) {
            this.logger.warn(`Filtering out invalid size:`, JSON.stringify(size));
          }
          return isValid;
        }
      );

      this.logger.info(`After filtering - Total sizes: ${this.currentSizes.length}, Valid sizes: ${validSizes.length}`);
      if (validSizes.length > 0) {
        this.logger.info('Valid sizes to send:', JSON.stringify(validSizes, null, 2));
      } else {
        this.logger.warn('No valid sizes found! All sizes were filtered out.');
      }

      const itemData: any = {
        ...this.menuItemForm.value,
        sizes: validSizes.length > 0 ? validSizes : [],
        addOnIds: this.selectedAddOnIds
      };
      
      this.logger.info('Sending menu item data:', {
        id: this.editingMenuItem?.id,
        nameEn: itemData.nameEn,
        sizesCount: itemData.sizes?.length || 0,
        sizes: JSON.stringify(itemData.sizes, null, 2),
        addOnsCount: itemData.addOnIds?.length || 0
      });

      // Convert image key to base64 if it's stored locally
      if (itemData.imageUrl && itemData.imageUrl.startsWith('image_')) {
        const base64Image = this.imageUploadService.getImageFromLocalStorage(itemData.imageUrl);
        if (base64Image) {
          itemData.imageUrl = base64Image;
        }
      }
      
      if (this.editingMenuItem?.id) {
        this.menuService.updateMenuItem(this.editingMenuItem.id, itemData).subscribe({
          next: () => {
            this.logger.info('Menu item updated successfully');
            this.resetMenuItemForm();
            this.loadMenuItems();
          },
          error: (err) => {
            this.logger.error('Error updating menu item:', err);
            alert('Failed to update menu item: ' + (err.error?.message || 'Unknown error'));
          }
        });
      } else {
        this.menuService.createMenuItem(itemData).subscribe({
          next: () => {
            this.logger.info('Menu item created successfully');
            this.resetMenuItemForm();
            this.loadMenuItems();
          },
          error: (err) => {
            this.logger.error('Error creating menu item:', err);
            alert('Failed to create menu item: ' + (err.error?.message || 'Unknown error'));
          }
        });
      }
    } else {
      alert('Please fill in all required fields');
    }
  }

  deleteMenuItem(id: number): void {
    if (confirm('Are you sure you want to delete this menu item?')) {
      this.menuService.deleteMenuItem(id).subscribe({
        next: () => {
          this.logger.info('Menu item deleted successfully');
          this.loadMenuItems();
        },
        error: (err) => {
          this.logger.error('Error deleting menu item:', err);
          alert('Failed to delete menu item: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  private resetMenuItemForm(): void {
    this.editingMenuItem = null;
    this.showMenuItemForm = false;
    this.menuItemImagePreview = null;
    this.menuItemImageFile = null;
    this.menuItemForm.reset({ isAvailable: true, isCombo: false, displayOrder: 0 });
    this.currentSizes = [];
    this.selectedAddOnIds = [];
  }

  // Add-ons
  loadAddOns(): void {
    this.menuService.getAddOns().subscribe({
      next: (data) => {
        this.addOns = data;
        this.logger.debug(`Loaded ${data.length} add-ons`);
      },
      error: (err) => {
        this.logger.error('Error loading add-ons:', err);
        this.addOns = [];
      }
    });
  }

  openAddOnForm(addOn?: AddOn): void {
    this.showAddOnFormFlag = true;
    this.editingAddOn = addOn || null;
    if (addOn) {
      this.addOnForm.patchValue(addOn);
    } else {
      this.addOnForm.reset({ isAvailable: true, price: 0, displayOrder: 0 });
    }
  }

  saveAddOn(): void {
    if (this.addOnForm.valid) {
      const addOnData = this.addOnForm.value;
      if (this.editingAddOn?.id) {
        this.menuService.updateAddOn(this.editingAddOn.id, addOnData).subscribe({
          next: () => {
            this.logger.info('Add-on updated successfully');
            this.resetAddOnForm();
            this.loadAddOns();
          },
          error: (err) => {
            this.logger.error('Error updating add-on:', err);
            alert('Failed to update add-on: ' + (err.error?.message || 'Unknown error'));
          }
        });
      } else {
        this.menuService.createAddOn(addOnData).subscribe({
          next: () => {
            this.logger.info('Add-on created successfully');
            this.resetAddOnForm();
            this.loadAddOns();
          },
          error: (err) => {
            this.logger.error('Error creating add-on:', err);
            alert('Failed to create add-on: ' + (err.error?.message || 'Unknown error'));
          }
        });
      }
    } else {
      alert('Please fill in all required fields');
    }
  }

  deleteAddOn(id: number): void {
    if (confirm('Are you sure you want to delete this add-on?')) {
      this.menuService.deleteAddOn(id).subscribe({
        next: () => {
          this.logger.info('Add-on deleted successfully');
          this.loadAddOns();
        },
        error: (err) => {
          this.logger.error('Error deleting add-on:', err);
          alert('Failed to delete add-on: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  private resetAddOnForm(): void {
    this.editingAddOn = null;
    this.showAddOnFormFlag = false;
    this.addOnForm.reset({ isAvailable: true, price: 0, displayOrder: 0 });
  }

  // Combos
  loadCombos(): void {
    this.menuService.getCombos().subscribe({
      next: (data) => {
        this.combos = data;
        this.logger.debug(`Loaded ${data.length} combos`);
      },
      error: (err) => {
        this.logger.error('Error loading combos:', err);
        this.combos = [];
      }
    });
  }

  openComboForm(combo?: Combo): void {
    this.showComboForm = true;
    this.editingCombo = combo || null;
    this.comboItems = combo?.items?.map((item: ComboItem) => ({
      menuItemId: item.menuItemId,
      quantity: item.quantity
    })) || [];
    this.comboImagePreview = null;
    this.comboImageFile = null;
    
    if (combo) {
      this.comboForm.patchValue({
        nameEn: combo.nameEn,
        nameUr: combo.nameUr,
        descriptionEn: combo.descriptionEn,
        descriptionUr: combo.descriptionUr,
        comboPrice: combo.comboPrice,
        imageUrl: combo.imageUrl,
        isAvailable: combo.isAvailable ?? true,
        displayOrder: combo.displayOrder ?? 0
      });
      if (combo.imageUrl) {
        this.comboImagePreview = combo.imageUrl;
      }
    } else {
      this.comboForm.reset({ isAvailable: true, displayOrder: 0 });
      this.comboItems = [];
    }
  }

  onComboImageSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const validation = this.imageUploadService.validateFile(file);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }

    this.comboImageFile = file;
    this.uploadingImage = true;

    this.imageUploadService.compressImage(file).then((base64) => {
      this.comboImagePreview = base64;
      const imageKey = this.imageUploadService.saveImageToLocalStorage(base64, 'combo');
      this.comboForm.patchValue({ imageUrl: imageKey });
      this.uploadingImage = false;
    }).catch((error) => {
      this.logger.error('Error compressing image:', error);
      alert('Failed to process image. Please try again.');
      this.uploadingImage = false;
    });
  }

  removeComboImage(): void {
    const imageUrl = this.comboForm.get('imageUrl')?.value;
    if (imageUrl && imageUrl.startsWith('image_')) {
      this.imageUploadService.deleteImageFromLocalStorage(imageUrl);
    }
    this.comboImagePreview = null;
    this.comboImageFile = null;
    this.comboForm.patchValue({ imageUrl: '' });
  }

  addComboItem(): void {
    this.comboItems.push({ menuItemId: 0, quantity: 1 });
  }

  removeComboItem(index: number): void {
    this.comboItems.splice(index, 1);
  }

  saveCombo(): void {
    if (this.comboForm.valid && this.comboItems.length > 0) {
      const comboData: any = {
        ...this.comboForm.value,
        items: this.comboItems.map((item: { menuItemId: number; quantity: number }) => ({
          menuItemId: item.menuItemId,
          quantity: item.quantity
        }))
      };

      // Convert image key to base64 if it's stored locally
      if (comboData.imageUrl && comboData.imageUrl.startsWith('image_')) {
        const base64Image = this.imageUploadService.getImageFromLocalStorage(comboData.imageUrl);
        if (base64Image) {
          comboData.imageUrl = base64Image;
        }
      }
      
      if (this.editingCombo?.id) {
        this.menuService.updateCombo(this.editingCombo.id, comboData).subscribe({
          next: () => {
            this.logger.info('Combo updated successfully');
            this.resetComboForm();
            this.loadCombos();
          },
          error: (err) => {
            this.logger.error('Error updating combo:', err);
            alert('Failed to update combo: ' + (err.error?.message || 'Unknown error'));
          }
        });
      } else {
        this.menuService.createCombo(comboData).subscribe({
          next: () => {
            this.logger.info('Combo created successfully');
            this.resetComboForm();
            this.loadCombos();
          },
          error: (err) => {
            this.logger.error('Error creating combo:', err);
            alert('Failed to create combo: ' + (err.error?.message || 'Unknown error'));
          }
        });
      }
    } else {
      if (!this.comboForm.valid) {
        alert('Please fill in all required fields');
      } else if (this.comboItems.length === 0) {
        alert('Please add at least one item to the combo');
      }
    }
  }

  deleteCombo(id: number): void {
    if (confirm('Are you sure you want to delete this combo?')) {
      this.menuService.deleteCombo(id).subscribe({
        next: () => {
          this.logger.info('Combo deleted successfully');
          this.loadCombos();
        },
        error: (err) => {
          this.logger.error('Error deleting combo:', err);
          alert('Failed to delete combo: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  private resetComboForm(): void {
    this.editingCombo = null;
    this.showComboForm = false;
    this.comboImagePreview = null;
    this.comboImageFile = null;
    this.comboForm.reset({ isAvailable: true, displayOrder: 0 });
    this.comboItems = [];
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category?.nameEn || 'Unknown';
  }

  getMenuItemName(menuItemId: number): string {
    const item = this.availableMenuItems.find(i => i.id === menuItemId);
    return item?.nameEn || 'Unknown';
  }
}

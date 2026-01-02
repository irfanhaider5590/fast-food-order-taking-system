import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { MenuCategory } from '../../../../models/menu.models';
import { MenuService } from '../../../../services/menu.service';
import { LoggerService } from '../../../../services/logger.service';
import { ImageUploadComponent } from '../../../../shared/components/image-upload/image-upload.component';
import { ImageUploadService } from '../../../../services/image-upload.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ImageUploadComponent],
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.css']
})
export class CategoryFormComponent implements OnInit {
  @Input() category: MenuCategory | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  categoryForm: FormGroup;
  imagePreview: string | null = null;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private menuService: MenuService,
    private logger: LoggerService,
    private imageUploadService: ImageUploadService
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
  }

  ngOnInit(): void {
    if (this.category) {
      this.categoryForm.patchValue(this.category);
      if (this.category.imageUrl) {
        this.imagePreview = this.category.imageUrl;
      }
    }
  }

  onImageSelected(imageKey: string): void {
    this.categoryForm.patchValue({ imageUrl: imageKey });
  }

  onImageRemoved(): void {
    this.categoryForm.patchValue({ imageUrl: '' });
    this.imagePreview = null;
  }

  save(): void {
    if (this.categoryForm.valid) {
      this.saving = true;
      const categoryData = { ...this.categoryForm.value };

      // Convert image key to base64 if stored locally
      if (categoryData.imageUrl && categoryData.imageUrl.startsWith('image_')) {
        const base64Image = this.imageUploadService.getImageFromLocalStorage(categoryData.imageUrl);
        if (base64Image) {
          categoryData.imageUrl = base64Image;
        }
      }

      const operation = this.category?.id
        ? this.menuService.updateCategory(this.category.id, categoryData)
        : this.menuService.createCategory(categoryData);

      operation.subscribe({
        next: () => {
          this.logger.info('Category saved successfully');
          this.saving = false;
          this.saved.emit();
        },
        error: (err) => {
          this.logger.error('Error saving category:', err);
          alert('Failed to save category: ' + (err.error?.message || 'Unknown error'));
          this.saving = false;
        }
      });
    } else {
      alert('Please fill in all required fields');
    }
  }

  cancel(): void {
    this.cancelled.emit();
  }
}


import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuCategory } from '../../../../models/menu.models';
import { MenuService } from '../../../../services/menu.service';
import { LoggerService } from '../../../../services/logger.service';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {
  @Input() categories: MenuCategory[] = [];
  @Output() edit = new EventEmitter<MenuCategory>();
  @Output() delete = new EventEmitter<number>();
  @Output() refresh = new EventEmitter<void>();

  constructor(
    private menuService: MenuService,
    private logger: LoggerService
  ) {}

  ngOnInit(): void {
    if (this.categories.length === 0) {
      this.loadCategories();
    }
  }

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

  onEdit(category: MenuCategory): void {
    this.edit.emit(category);
  }

  onDelete(id: number): void {
    if (confirm('Are you sure you want to delete this category?')) {
      this.menuService.deleteCategory(id).subscribe({
        next: () => {
          this.logger.info('Category deleted successfully');
          this.refresh.emit();
        },
        error: (err) => {
          this.logger.error('Error deleting category:', err);
          alert('Failed to delete category: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  onAddNew(): void {
    this.edit.emit(null as unknown as MenuCategory);
  }
}


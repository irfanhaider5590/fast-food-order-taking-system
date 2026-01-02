import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ImageUploadService } from '../../../services/image-upload.service';
import { FileUploadService } from '../../../services/file-upload.service';
import { LoggerService } from '../../../services/logger.service';

@Component({
  selector: 'app-image-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './image-upload.component.html',
  styleUrls: ['./image-upload.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ImageUploadComponent),
      multi: true
    }
  ]
})
export class ImageUploadComponent implements ControlValueAccessor {
  @Input() previewUrl: string | null = null;
  @Input() storageKey: string = 'default';
  @Input() label: string = 'Image';
  @Input() uploadToServer: boolean = true; // New input to control upload behavior
  @Output() imageSelected = new EventEmitter<string>();
  @Output() imageRemoved = new EventEmitter<void>();

  uploading = false;
  private onChange = (value: string) => {};
  private onTouched = () => {};

  constructor(
    private imageUploadService: ImageUploadService,
    private fileUploadService: FileUploadService,
    private logger: LoggerService
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const validation = this.imageUploadService.validateFile(file);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }

    this.uploading = true;

    if (this.uploadToServer) {
      // Upload to server
      this.fileUploadService.uploadImage(file, this.storageKey).subscribe({
        next: (response) => {
          this.previewUrl = response.url;
          this.onChange(response.url);
          this.imageSelected.emit(response.url);
          this.uploading = false;
          this.logger.info('Image uploaded successfully:', response.url);
        },
        error: (error) => {
          this.logger.error('Error uploading image:', error);
          alert('Failed to upload image. Please try again.');
          this.uploading = false;
        }
      });
    } else {
      // Use local storage (legacy behavior)
      this.imageUploadService.compressImage(file)
        .then((base64) => {
          this.previewUrl = base64;
          const imageKey = this.imageUploadService.saveImageToLocalStorage(base64, this.storageKey);
          this.onChange(imageKey);
          this.imageSelected.emit(imageKey);
          this.uploading = false;
        })
        .catch((error) => {
          this.logger.error('Error compressing image:', error);
          alert('Failed to process image. Please try again.');
          this.uploading = false;
        });
    }
  }

  removeImage(): void {
    if (this.previewUrl) {
      if (this.previewUrl.startsWith('image_')) {
        // Local storage image
        this.imageUploadService.deleteImageFromLocalStorage(this.previewUrl);
      } else if (this.previewUrl.startsWith('/assets/')) {
        // Server image - delete from server
        this.fileUploadService.deleteImage(this.previewUrl).subscribe({
          next: () => {
            this.logger.info('Image deleted from server:', this.previewUrl);
          },
          error: (error) => {
            this.logger.error('Error deleting image from server:', error);
            // Continue with removal even if server delete fails
          }
        });
      }
    }
    this.previewUrl = null;
    this.onChange('');
    this.imageRemoved.emit();
  }

  // ControlValueAccessor implementation
  writeValue(value: string): void {
    if (!value) {
      this.previewUrl = null;
      return;
    }
    
    if (value.startsWith('image_')) {
      // Local storage image
      const base64Image = this.imageUploadService.getImageFromLocalStorage(value);
      this.previewUrl = base64Image || value;
    } else if (value.startsWith('/assets/')) {
      // Server image path - use as is
      this.previewUrl = value;
    } else if (value.startsWith('data:image/')) {
      // Base64 image
      this.previewUrl = value;
    } else {
      // Assume it's a relative path, add /assets/images/ prefix if needed
      if (!value.startsWith('/')) {
        this.previewUrl = '/assets/images/' + value;
      } else {
        this.previewUrl = value;
      }
    }
    this.logger.debug('ImageUploadComponent.writeValue() - Set previewUrl:', this.previewUrl);
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
}


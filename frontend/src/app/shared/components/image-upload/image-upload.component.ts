import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ImageUploadService } from '../../../services/image-upload.service';
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
  @Output() imageSelected = new EventEmitter<string>();
  @Output() imageRemoved = new EventEmitter<void>();

  uploading = false;
  private onChange = (value: string) => {};
  private onTouched = () => {};

  constructor(
    private imageUploadService: ImageUploadService,
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

  removeImage(): void {
    if (this.previewUrl && this.previewUrl.startsWith('image_')) {
      this.imageUploadService.deleteImageFromLocalStorage(this.previewUrl);
    }
    this.previewUrl = null;
    this.onChange('');
    this.imageRemoved.emit();
  }

  // ControlValueAccessor implementation
  writeValue(value: string): void {
    if (value && value.startsWith('image_')) {
      const base64Image = this.imageUploadService.getImageFromLocalStorage(value);
      this.previewUrl = base64Image || value;
    } else {
      this.previewUrl = value || null;
    }
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
}


import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ImageUploadService {
  private readonly MAX_WIDTH = 800;
  private readonly MAX_HEIGHT = 600;
  private readonly QUALITY = 0.8;
  private readonly MAX_SIZE_KB = 200;

  /**
   * Compress and convert image to base64
   */
  async compressImage(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = (e: any) => {
        const img = new Image();
        
        img.onload = () => {
          const canvas = document.createElement('canvas');
          let width = img.width;
          let height = img.height;

          // Calculate new dimensions
          if (width > height) {
            if (width > this.MAX_WIDTH) {
              height = (height * this.MAX_WIDTH) / width;
              width = this.MAX_WIDTH;
            }
          } else {
            if (height > this.MAX_HEIGHT) {
              width = (width * this.MAX_HEIGHT) / height;
              height = this.MAX_HEIGHT;
            }
          }

          canvas.width = width;
          canvas.height = height;

          const ctx = canvas.getContext('2d');
          if (!ctx) {
            reject(new Error('Could not get canvas context'));
            return;
          }

          ctx.drawImage(img, 0, 0, width, height);

          // Convert to blob and check size
          canvas.toBlob(
            (blob) => {
              if (!blob) {
                reject(new Error('Failed to compress image'));
                return;
              }

              // If still too large, reduce quality further
              if (blob.size > this.MAX_SIZE_KB * 1024) {
                canvas.toBlob(
                  (finalBlob) => {
                    if (!finalBlob) {
                      reject(new Error('Failed to compress image'));
                      return;
                    }
                    this.blobToBase64(finalBlob).then(resolve).catch(reject);
                  },
                  'image/jpeg',
                  0.6
                );
              } else {
                this.blobToBase64(blob).then(resolve).catch(reject);
              }
            },
            'image/jpeg',
            this.QUALITY
          );
        };

        img.onerror = () => reject(new Error('Failed to load image'));
        img.src = e.target.result;
      };

      reader.onerror = () => reject(new Error('Failed to read file'));
      reader.readAsDataURL(file);
    });
  }

  /**
   * Convert blob to base64 string
   */
  private blobToBase64(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result);
        } else {
          reject(new Error('Failed to convert blob to base64'));
        }
      };
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

  /**
   * Save image to local storage and return path
   */
  saveImageToLocalStorage(base64Image: string, prefix: string): string {
    const timestamp = Date.now();
    const key = `image_${prefix}_${timestamp}`;
    localStorage.setItem(key, base64Image);
    return key;
  }

  /**
   * Get image from local storage
   */
  getImageFromLocalStorage(key: string): string | null {
    return localStorage.getItem(key);
  }

  /**
   * Delete image from local storage
   */
  deleteImageFromLocalStorage(key: string): void {
    localStorage.removeItem(key);
  }

  /**
   * Get all image keys from local storage
   */
  getAllImageKeys(): string[] {
    const keys: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('image_')) {
        keys.push(key);
      }
    }
    return keys;
  }

  /**
   * Validate file before upload
   */
  validateFile(file: File): { valid: boolean; error?: string } {
    // Check file type
    if (!file.type.startsWith('image/')) {
      return { valid: false, error: 'Please select an image file' };
    }

    // Check file size (before compression)
    const maxSizeMB = 5;
    if (file.size > maxSizeMB * 1024 * 1024) {
      return { valid: false, error: `File size must be less than ${maxSizeMB}MB` };
    }

    return { valid: true };
  }

  /**
   * Get file size in KB
   */
  getFileSizeKB(file: File): number {
    return Math.round(file.size / 1024);
  }
}


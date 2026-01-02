import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SettingsService } from '../../services/settings.service';
import { Settings, SettingsRequest } from '../../models/settings.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';
import { ImageUploadComponent } from '../../shared/components/image-upload/image-upload.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ImageUploadComponent],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  settings: Settings = {
    brandName: '',
    brandLogoUrl: '',
    contactPhone: '',
    contactEmail: '',
    address: ''
  };
  
  loading = false;
  saving = false;
  error: string | null = null;

  constructor(
    private settingsService: SettingsService,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.loading = true;
    this.error = null;
    this.logger.debug('SettingsComponent.loadSettings() - Starting to load settings');
    this.settingsService.getSettings().subscribe({
      next: (settings) => {
        this.logger.debug('SettingsComponent.loadSettings() - Received settings:', JSON.stringify(settings));
        this.settings = {
          id: settings.id,
          brandName: settings.brandName || '',
          brandLogoUrl: settings.brandLogoUrl || '',
          contactPhone: settings.contactPhone || '',
          contactEmail: settings.contactEmail || '',
          address: settings.address || '',
          createdAt: settings.createdAt,
          updatedAt: settings.updatedAt,
          createdByUsername: settings.createdByUsername,
          updatedByUsername: settings.updatedByUsername
        };
        this.loading = false;
        this.logger.info('Settings loaded successfully. Brand name:', this.settings.brandName);
        this.logger.debug('SettingsComponent.loadSettings() - Settings object after assignment:', JSON.stringify(this.settings));
        this.cdr.detectChanges(); // Force change detection
      },
      error: (err) => {
        this.error = 'Failed to load settings';
        this.loading = false;
        this.logger.error('Error loading settings:', err);
        this.notificationService.showError('Failed to load settings', 3000);
        this.cdr.detectChanges();
      }
    });
  }

  onLogoSelected(imageUrl: string): void {
    this.settings.brandLogoUrl = imageUrl;
    this.logger.debug('Logo selected, URL:', imageUrl);
  }

  onLogoRemoved(): void {
    this.settings.brandLogoUrl = '';
    this.logger.debug('Logo removed');
  }

  getImageUrl(path: string | null | undefined): string | null {
    if (!path) return null;
    
    // If path already starts with http:// or https://, return as is
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path;
    }
    
    // Backend base URL (without /api suffix)
    const backendBaseUrl = environment.apiUrl.replace('/api', '');
    
    // If path starts with /api/files/serve, prepend backend URL
    if (path.startsWith('/api/files/serve')) {
      return `${backendBaseUrl}${path}`;
    }
    
    // If path starts with /assets/images/, convert to API endpoint
    if (path.startsWith('/assets/images/')) {
      return `${backendBaseUrl}/api/files/serve?path=${encodeURIComponent(path)}`;
    }
    
    // If path doesn't start with /, add /assets/images/ prefix first
    if (!path.startsWith('/')) {
      const fullPath = '/assets/images/' + path;
      return `${backendBaseUrl}/api/files/serve?path=${encodeURIComponent(fullPath)}`;
    }
    
    // Default: assume it's an assets path
    return `${backendBaseUrl}/api/files/serve?path=${encodeURIComponent(path)}`;
  }

  saveSettings(): void {
    if (!this.settings.brandName) {
      this.notificationService.showWarning('Brand name is required', 3000);
      return;
    }

    this.saving = true;
    const settingsRequest: SettingsRequest = {
      brandName: this.settings.brandName,
      brandLogoUrl: this.settings.brandLogoUrl,
      contactPhone: this.settings.contactPhone,
      contactEmail: this.settings.contactEmail,
      address: this.settings.address
    };

    this.settingsService.updateSettings(settingsRequest).subscribe({
      next: (updatedSettings) => {
        this.settings = updatedSettings;
        this.saving = false;
        this.notificationService.showSuccess('Settings saved successfully', 3000);
        this.logger.info('Settings saved successfully');
        // Reload the page to update header with new brand name and logo
        setTimeout(() => {
          window.location.reload();
        }, 1000);
      },
      error: (err) => {
        this.saving = false;
        this.logger.error('Error saving settings:', err);
        this.notificationService.showError(
          'Failed to save settings: ' + (err.error?.message || 'Unknown error'),
          4000
        );
      }
    });
  }
}


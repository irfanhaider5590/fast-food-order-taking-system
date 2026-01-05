import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SettingsService } from '../../services/settings.service';
import { Settings, SettingsRequest } from '../../models/settings.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';
import { ImageUploadComponent } from '../../shared/components/image-upload/image-upload.component';
import { LicenseService, LicenseStatus } from '../../services/license.service';
import { LicenseGuardService } from '../../services/license-guard.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ImageUploadComponent],
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

  // License Management
  licenseStatus: LicenseStatus | null = null;
  showLicenseSection = false;
  licenseForm: FormGroup;
  isAdmin = false; // Track if current user is admin
  currentUser: any = null; // Store current user info

  constructor(
    private settingsService: SettingsService,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef,
    private licenseService: LicenseService,
    private licenseGuard: LicenseGuardService,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.licenseForm = this.fb.group({
      licenseKey: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Check if user is admin
    this.checkAdminRole();
    this.loadSettings();
    // Force refresh license status to get latest from backend
    this.licenseGuard.forceRefresh();
    // Then load the status
    setTimeout(() => {
      this.loadLicenseStatus();
    }, 500); // Wait a bit for API call to complete
  }

  checkAdminRole(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      // Check if user is admin based on role, roleName, or roleId
      // User object has 'role' field (not 'roleName')
      const role = this.currentUser?.role || this.currentUser?.roleName;
      const roleId = this.currentUser?.roleId;
      
      this.isAdmin = role === 'ADMIN' || 
                     role === 'Admin' ||
                     role?.toLowerCase() === 'admin' ||
                     roleId === 1; // Adjust roleId based on your system
      this.logger.debug('User role check - isAdmin:', this.isAdmin, 'role:', this.currentUser?.role, 'roleName:', this.currentUser?.roleName, 'roleId:', this.currentUser?.roleId);
    }
  }

  loadLicenseStatus(): void {
    // Use setTimeout to avoid NG0100 error
    setTimeout(() => {
      // Get current status immediately if available
      const currentStatus = this.licenseGuard.getCurrentStatus();
      if (currentStatus) {
        this.licenseStatus = currentStatus;
        this.showLicenseSection = !currentStatus.isValid;
        this.cdr.detectChanges();
        
        // Don't show notification here - AppComponent handles it globally
      }
      
      // Subscribe to LicenseGuardService to get updates
      this.licenseGuard.licenseStatus$.pipe(
        take(1) // Only take first emission
      ).subscribe({
        next: (status) => {
          if (status) {
            setTimeout(() => {
              this.licenseStatus = status;
              this.showLicenseSection = !status.isValid;
              this.cdr.detectChanges();
              
              // Don't show notification here - AppComponent handles it globally
            }, 0);
          }
        },
        error: (err) => {
          this.logger.error('Error loading license status:', err);
        }
      });
    }, 0);
  }

  refreshLicenseStatus(): void {
    // Force refresh from backend
    this.licenseGuard.forceRefresh();
    
    // Wait a bit for API call to complete, then reload status
    setTimeout(() => {
      this.loadLicenseStatus();
    }, 1000);
  }

  activateLicense(): void {
    if (this.licenseForm.invalid) {
      return;
    }

    const licenseKey = this.licenseForm.get('licenseKey')?.value;
    this.saving = true;

    this.licenseService.activateLicense(licenseKey).subscribe({
      next: (status) => {
        this.licenseStatus = status;
        this.saving = false;
        this.licenseForm.reset();
        
        // Update the guard service status directly (don't trigger new API call)
        this.licenseGuard.updateStatus(status);
        
        if (status.isValid) {
          this.notificationService.showSuccess('License activated successfully! All modules are now available.', 5000);
          this.showLicenseSection = false;
          
          // Reload page after 1 second to refresh dashboard and show all modules
          setTimeout(() => {
            window.location.reload();
          }, 1000);
        } else {
          this.notificationService.showError(status.message, 8000);
        }
      },
      error: (err) => {
        this.saving = false;
        const errorMessage = err.error?.message || 'Failed to activate license';
        this.notificationService.showError(errorMessage, 8000);
      }
    });
  }


  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
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


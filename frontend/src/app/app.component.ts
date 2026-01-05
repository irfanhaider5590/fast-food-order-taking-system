import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationComponent } from './shared/components/notification/notification.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { SettingsService } from './services/settings.service';
import { Settings } from './models/settings.models';
import { LoggerService } from './services/logger.service';
import { StockService } from './services/stock.service';
import { NotificationService } from './services/notification.service';
import { StockWarning } from './models/stock.models';
import { LicenseService } from './services/license.service';
import { LicenseGuardService } from './services/license-guard.service';
import { Subscription, interval } from 'rxjs';
import { take } from 'rxjs/operators';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, TranslateModule, NotificationComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Fast Food Order System';
  currentLang = 'en';
  brandName: string = 'Fast Food Order System';
  brandLogoUrl: string | null = null;
  licenseWarningMessage: string | null = null; // Banner warning message
  private settingsSubscription?: Subscription;
  private stockWarningSubscription?: Subscription;
  private licenseWarningSubscription?: Subscription;
  private licenseStatusSubscription?: Subscription;
  private lastWarningCheck: Date | null = null;
  private lastLicenseWarningDay: number | null = null;

  constructor(
    private translate: TranslateService,
    private router: Router,
    private settingsService: SettingsService,
    private stockService: StockService,
    private notificationService: NotificationService,
    private licenseService: LicenseService,
    private licenseGuard: LicenseGuardService,
    private logger: LoggerService
  ) {
    this.translate.setDefaultLang('en');
    this.translate.use('en');
  }

  ngOnInit(): void {
    this.loadSettings();
    this.startStockWarningCheck();
    this.startLicenseWarningCheck();
    
    // Subscribe to license status for banner updates
    this.licenseStatusSubscription = this.licenseGuard.licenseStatus$.subscribe(status => {
      if (status) {
        this.processLicenseWarning(status);
      }
    });
    
    // Initial check
    if (!this.licenseGuard.getCurrentStatus()) {
      this.licenseGuard.checkLicenseStatus();
    } else {
      this.processLicenseWarning(this.licenseGuard.getCurrentStatus()!);
    }
  }

  ngOnDestroy(): void {
    if (this.settingsSubscription) {
      this.settingsSubscription.unsubscribe();
    }
    if (this.stockWarningSubscription) {
      this.stockWarningSubscription.unsubscribe();
    }
    if (this.licenseWarningSubscription) {
      this.licenseWarningSubscription.unsubscribe();
    }
    if (this.licenseStatusSubscription) {
      this.licenseStatusSubscription.unsubscribe();
    }
  }

  startStockWarningCheck(): void {
    // Check immediately
    this.checkStockWarnings();
    
    // Then check every 2 hours (7200000 milliseconds)
    this.stockWarningSubscription = interval(7200000).subscribe(() => {
      this.checkStockWarnings();
    });
  }

  checkStockWarnings(): void {
    if (!this.isLoggedIn()) {
      return;
    }

    // Check if alerts are enabled before showing notifications
    this.stockService.getAlertsEnabled().subscribe({
      next: (config) => {
        if (!config.alertsEnabled) {
          this.logger.debug('Stock alerts are disabled, skipping notifications');
          return;
        }

        this.stockService.getStockWarnings().subscribe({
          next: (warnings: StockWarning[]) => {
            if (warnings.length > 0) {
              // Show warnings in both English and Urdu
              warnings.forEach(warning => {
                this.notificationService.showWarning(
                  `${warning.warningMessageEn}\n${warning.warningMessageUr}`,
                  10000 // Show for 10 seconds
                );
              });
              this.lastWarningCheck = new Date();
            }
          },
          error: (err) => {
            this.logger.error('Error checking stock warnings:', err);
          }
        });
      },
      error: (err) => {
        this.logger.error('Error checking alerts enabled status:', err);
        // If check fails, proceed with showing warnings (default behavior)
        this.stockService.getStockWarnings().subscribe({
          next: (warnings: StockWarning[]) => {
            if (warnings.length > 0) {
              warnings.forEach(warning => {
                this.notificationService.showWarning(
                  `${warning.warningMessageEn}\n${warning.warningMessageUr}`,
                  10000
                );
              });
              this.lastWarningCheck = new Date();
            }
          },
          error: (err2) => {
            this.logger.error('Error checking stock warnings:', err2);
          }
        });
      }
    });
  }

  startLicenseWarningCheck(): void {
    // Check immediately
    this.checkLicenseWarnings();
    
    // Then check every hour (3600000 milliseconds)
    this.licenseWarningSubscription = interval(3600000).subscribe(() => {
      this.checkLicenseWarnings();
    });
  }

  checkLicenseWarnings(): void {
    if (!this.isLoggedIn()) {
      return;
    }

    // Use LicenseGuardService to avoid duplicate API calls
    const currentStatus = this.licenseGuard.getCurrentStatus();
    if (currentStatus) {
      this.processLicenseWarning(currentStatus);
    } else {
      // If no status available, subscribe to get it once
      this.licenseGuard.licenseStatus$.pipe(
        take(1) // Only take first emission to avoid continuous subscription
      ).subscribe({
        next: (status) => {
          if (status) {
            this.processLicenseWarning(status);
          }
        }
      });
      // Trigger check if needed
      this.licenseGuard.checkLicenseStatus();
    }
  }

  private processLicenseWarning(status: any): void {
    // Check if current user is admin
    const userStr = localStorage.getItem('user');
    let isAdmin = false;
    if (userStr) {
      const user = JSON.parse(userStr);
      // Check if user is admin based on role, roleName, or roleId
      // User object has 'role' field (not 'roleName')
      const role = user?.role || user?.roleName;
      const roleId = user?.roleId;
      
      isAdmin = role === 'ADMIN' || 
                role === 'Admin' ||
                role?.toLowerCase() === 'admin' ||
                roleId === 1;
    }

    // Show banner warning if 15 days or less remaining
    if (status.isValid && status.daysRemaining <= 15 && status.daysRemaining >= 0) {
      if (status.shouldShowWarning && status.warningMessage) {
        this.licenseWarningMessage = status.warningMessage;
        
        // Also show notification if it's a different day than last warning
        if (this.lastLicenseWarningDay !== status.daysRemaining) {
          this.notificationService.showWarning(
            status.warningMessage,
            15000 // Show for 15 seconds
          );
          this.lastLicenseWarningDay = status.daysRemaining;
        }
      } else {
        // Show banner even if not in exact warning days, but <= 15 days
        if (isAdmin) {
          this.licenseWarningMessage = `Warning: Your license will expire in ${status.daysRemaining} day(s). Please renew your license before expiration.`;
        } else {
          this.licenseWarningMessage = `Warning: License will expire in ${status.daysRemaining} day(s). Please contact your administrator to renew the license.`;
        }
      }
    } else {
      // Hide banner if license is valid and more than 15 days remaining
      if (status.isValid && status.daysRemaining > 15) {
        this.licenseWarningMessage = null;
        this.lastLicenseWarningDay = null;
      } else if (!status.isValid) {
        // Show banner if license is invalid - different messages for admin vs non-admin
        if (isAdmin) {
          this.licenseWarningMessage = status.message || 'License is invalid or expired. Please go to Settings to activate a valid license.';
        } else {
          this.licenseWarningMessage = 'License is invalid or expired. Please contact your administrator to renew the license.';
        }
      }
    }
  }

  dismissWarning(): void {
    this.licenseWarningMessage = null;
  }

  loadSettings(): void {
    if (!this.isLoggedIn()) {
      return; // Don't load settings if not logged in
    }

    this.settingsSubscription = this.settingsService.getSettings().subscribe({
      next: (settings: Settings) => {
        this.brandName = settings.brandName || 'Fast Food Order System';
        this.brandLogoUrl = this.getImageUrl(settings.brandLogoUrl);
        this.logger.debug('AppComponent.loadSettings() - Loaded settings:', {
          brandName: this.brandName,
          brandLogoUrl: this.brandLogoUrl,
          originalPath: settings.brandLogoUrl
        });
      },
      error: (err) => {
        this.logger.error('AppComponent.loadSettings() - Error loading settings:', err);
        // Keep default values on error
      }
    });
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

  switchLanguage(lang: string) {
    this.currentLang = lang;
    this.translate.use(lang);
    document.documentElement.setAttribute('dir', lang === 'ur' ? 'rtl' : 'ltr');
  }

  goHome(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    // Clear authentication tokens
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    
    // Navigate to login page
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('accessToken');
  }
}


import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationComponent } from './shared/components/notification/notification.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { SettingsService } from './services/settings.service';
import { Settings } from './models/settings.models';
import { LoggerService } from './services/logger.service';
import { Subscription } from 'rxjs';
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
  private settingsSubscription?: Subscription;

  constructor(
    private translate: TranslateService,
    private router: Router,
    private settingsService: SettingsService,
    private logger: LoggerService
  ) {
    this.translate.setDefaultLang('en');
    this.translate.use('en');
  }

  ngOnInit(): void {
    this.loadSettings();
  }

  ngOnDestroy(): void {
    if (this.settingsSubscription) {
      this.settingsSubscription.unsubscribe();
    }
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


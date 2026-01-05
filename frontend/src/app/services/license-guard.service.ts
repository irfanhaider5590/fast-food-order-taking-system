import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { distinctUntilChanged } from 'rxjs/operators';
import { LicenseService, LicenseStatus } from './license.service';

@Injectable({
  providedIn: 'root'
})
export class LicenseGuardService {
  private licenseStatusSubject = new BehaviorSubject<LicenseStatus | null>(null);
  public licenseStatus$: Observable<LicenseStatus | null> = this.licenseStatusSubject.asObservable().pipe(
    distinctUntilChanged((prev, curr) => {
      // Only emit if status actually changed
      if (!prev && !curr) return true;
      if (!prev || !curr) return false;
      return prev.isValid === curr.isValid && 
             prev.isActivated === curr.isActivated &&
             prev.daysRemaining === curr.daysRemaining &&
             prev.machineId === curr.machineId;
    })
  );
  private isChecking = false; // Prevent concurrent checks
  private lastCheckTime = 0; // Track last check time
  private readonly CHECK_INTERVAL = 60000; // Minimum 60 seconds between checks

  constructor(private licenseService: LicenseService) {
    // Don't check immediately - let components request it when needed
    // This prevents multiple simultaneous requests on app startup
  }

  checkLicenseStatus(): void {
    // Prevent concurrent checks
    if (this.isChecking) {
      console.log('License check already in progress, skipping...');
      return;
    }
    
    // Prevent too frequent checks (throttle)
    const now = Date.now();
    if (this.lastCheckTime > 0 && (now - this.lastCheckTime) < this.CHECK_INTERVAL) {
      console.log('License check throttled - too soon since last check');
      return;
    }
    
    this.isChecking = true;
    this.lastCheckTime = now;
    
    // Check if token exists
    const token = localStorage.getItem('accessToken');
    console.log('Checking license status - Token exists:', !!token);
    
    this.licenseService.getLicenseStatus().subscribe({
      next: (status) => {
        console.log('License status received:', status);
        this.licenseStatusSubject.next(status);
        this.isChecking = false;
      },
      error: (err) => {
        console.error('Error checking license status:', err);
        console.error('Error details:', {
          status: err.status,
          statusText: err.statusText,
          message: err.message,
          error: err.error
        });
        // Default to invalid if check fails
        this.licenseStatusSubject.next({
          isActivated: false,
          isValid: false,
          daysRemaining: 0,
          message: 'Unable to verify license status',
          machineId: ''
        });
        this.isChecking = false;
      }
    });
  }

  isLicenseValid(): boolean {
    const status = this.licenseStatusSubject.value;
    return status?.isValid === true;
  }

  getCurrentStatus(): LicenseStatus | null {
    return this.licenseStatusSubject.value;
  }

  refreshStatus(): void {
    // Force refresh by resetting last check time
    this.lastCheckTime = 0;
    this.checkLicenseStatus();
  }

  // Force refresh ignoring throttling (for manual refresh)
  forceRefresh(): void {
    this.lastCheckTime = 0;
    this.isChecking = false;
    this.checkLicenseStatus();
  }

  // Update status directly without API call (useful after activation)
  updateStatus(status: LicenseStatus): void {
    console.log('Updating license status directly:', status);
    this.licenseStatusSubject.next(status);
  }
}


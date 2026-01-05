import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LicenseGuardService } from '../../services/license-guard.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  
  user: any;
  isLicenseValid = false; // Start with false, will be updated from service
  isAdmin = false; // Track if current user is admin
  private licenseStatusSubscription?: Subscription;

  constructor(
    private router: Router,
    private licenseGuard: LicenseGuardService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
      this.checkAdminRole();
    } else {
      this.router.navigate(['/login']);
      return;
    }

    // Defer initial check to avoid change detection issues
    setTimeout(() => {
      // Get current status first (if available)
      const currentStatus = this.licenseGuard.getCurrentStatus();
      if (currentStatus) {
        this.isLicenseValid = currentStatus.isValid === true;
        this.cdr.detectChanges();
      } else {
        // Only check if status is not available (initial load)
        this.licenseGuard.checkLicenseStatus();
      }
    }, 0);

    // Subscribe to license status changes (distinctUntilChanged is already in service)
    this.licenseStatusSubscription = this.licenseGuard.licenseStatus$.subscribe(status => {
      if (!status) {
        return; // Wait for initial status
      }
      
      // Use setTimeout to defer change detection
      setTimeout(() => {
        const wasValid = this.isLicenseValid;
        
        // Debug: Log the full status object
        console.log('License status received in dashboard:', status);
        console.log('status?.isValid:', status?.isValid);
        console.log('status?.isValid === true:', status?.isValid === true);
        console.log('typeof status?.isValid:', typeof status?.isValid);
        
        this.isLicenseValid = status?.isValid === true;
        this.cdr.detectChanges();
        
        console.log('License status changed:', { 
          wasValid, 
          isNowValid: this.isLicenseValid, 
          isAdmin: this.isAdmin,
          status,
          user: this.user
        });
        
        // If license just became valid, refresh the page to show all modules
        if (!wasValid && this.isLicenseValid) {
          console.log('License became valid, reloading page...');
          setTimeout(() => {
            window.location.reload();
          }, 500);
        }
      }, 0);
    });
  }

  ngOnDestroy(): void {
    if (this.licenseStatusSubscription) {
      this.licenseStatusSubscription.unsubscribe();
    }
  }

  navigateTo(path: string) {
    this.router.navigate([path]);
  }

  checkAdminRole(): void {
    // Check if user is admin based on role, roleName, or roleId
    // User object has 'role' field (not 'roleName')
    const role = this.user?.role || this.user?.roleName;
    const roleId = this.user?.roleId;
    
    this.isAdmin = role === 'ADMIN' || 
                   role === 'Admin' ||
                   role?.toLowerCase() === 'admin' ||
                   roleId === 1; // Adjust roleId based on your system
                   
    console.log('Dashboard - User role check:', {
      isAdmin: this.isAdmin,
      role: this.user?.role,
      roleName: this.user?.roleName,
      roleId: this.user?.roleId,
      fullUser: this.user
    });
    this.cdr.detectChanges(); // Force change detection after admin check
  }

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}


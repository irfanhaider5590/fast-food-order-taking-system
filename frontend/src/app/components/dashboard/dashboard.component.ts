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
  /** null = not loaded yet (avoids false→true reload loop) */
  isLicenseValid: boolean | null = null;
  isAdmin = false;
  private licenseStatusSubscription?: Subscription;
  private hasLoadedLicenseOnce = false;

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

    setTimeout(() => {
      const currentStatus = this.licenseGuard.getCurrentStatus();
      if (currentStatus) {
        this.isLicenseValid = currentStatus.isValid === true;
        this.hasLoadedLicenseOnce = true;
        this.cdr.detectChanges();
      } else {
        this.licenseGuard.checkLicenseStatus();
      }
    }, 0);

    this.licenseStatusSubscription = this.licenseGuard.licenseStatus$.subscribe(status => {
      if (!status) {
        return;
      }

      setTimeout(() => {
        const previous = this.isLicenseValid;
        this.isLicenseValid = status.isValid === true;
        this.cdr.detectChanges();

        // Only reload when license recovers after we already knew it was invalid
        // (e.g. after activation) — never on first load (previous === null).
        if (this.hasLoadedLicenseOnce && previous === false && this.isLicenseValid === true) {
          setTimeout(() => window.location.reload(), 300);
        }
        this.hasLoadedLicenseOnce = true;
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


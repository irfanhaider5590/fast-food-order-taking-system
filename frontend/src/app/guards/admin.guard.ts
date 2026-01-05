import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      this.router.navigate(['/login']);
      return false;
    }

    const user = JSON.parse(userStr);
    // Check if user is admin based on role, roleName, or roleId
    // User object has 'role' field (not 'roleName')
    const role = user?.role || user?.roleName;
    const roleId = user?.roleId;
    
    const isAdmin = role === 'ADMIN' || 
                    role === 'Admin' ||
                    role?.toLowerCase() === 'admin' ||
                    roleId === 1; // Adjust roleId based on your system

    if (!isAdmin) {
      // Redirect non-admin users to dashboard
      this.router.navigate(['/dashboard']);
      return false;
    }

    return true;
  }
}


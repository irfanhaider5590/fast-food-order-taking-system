import { Routes } from '@angular/router';
import { LicenseActivationComponent } from './components/license/license-activation.component';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { OrderTakingComponent } from './components/order-taking/order-taking.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { MenuManagementComponent } from './components/menu-management/menu-management.component';
import { OrderManagementComponent } from './components/order-management/order-management.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { SettingsComponent } from './components/settings/settings.component';
import { StockManagementComponent } from './components/stock-management/stock-management.component';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'license', component: LicenseActivationComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'orders', component: OrderTakingComponent },
  { path: 'order-management', component: OrderManagementComponent },
  // Admin only routes
  { path: 'analytics', component: AnalyticsComponent, canActivate: [AdminGuard] },
  { path: 'menu', component: MenuManagementComponent, canActivate: [AdminGuard] },
  { path: 'stock', component: StockManagementComponent, canActivate: [AdminGuard] },
  { path: 'user-management', component: UserManagementComponent, canActivate: [AdminGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [AdminGuard] }
];


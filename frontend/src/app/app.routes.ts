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
import { NotificationsComponent } from './components/notifications/notifications.component';
import { AdminGuard } from './guards/admin.guard';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'license', component: LicenseActivationComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrderTakingComponent, canActivate: [AuthGuard] },
  { path: 'order-management', component: OrderManagementComponent, canActivate: [AuthGuard] },
  { path: 'analytics', component: AnalyticsComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'menu', component: MenuManagementComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'stock', component: StockManagementComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'user-management', component: UserManagementComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [AuthGuard, AdminGuard] }
];


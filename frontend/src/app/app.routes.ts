import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { OrderTakingComponent } from './components/order-taking/order-taking.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { MenuManagementComponent } from './components/menu-management/menu-management.component';
import { OrderManagementComponent } from './components/order-management/order-management.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { SettingsComponent } from './components/settings/settings.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'orders', component: OrderTakingComponent },
  { path: 'order-management', component: OrderManagementComponent },
  { path: 'analytics', component: AnalyticsComponent },
  { path: 'menu', component: MenuManagementComponent },
  { path: 'user-management', component: UserManagementComponent },
  { path: 'settings', component: SettingsComponent }
];


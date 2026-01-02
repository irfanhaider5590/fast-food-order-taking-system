import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { OrderTakingComponent } from './components/order-taking/order-taking.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { MenuManagementComponent } from './components/menu-management/menu-management.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'orders', component: OrderTakingComponent },
  { path: 'analytics', component: AnalyticsComponent },
  { path: 'menu', component: MenuManagementComponent }
];


import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  @Input() showBack: boolean = false;
  @Input() showHome: boolean = true;
  @Input() showLogout: boolean = true;
  @Input() title: string = '';

  constructor(private router: Router) {}

  goBack(): void {
    window.history.back();
  }

  goHome(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    // Clear authentication tokens
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    
    // Navigate to login page
    this.router.navigate(['/login']);
  }
}


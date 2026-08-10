import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { StockAlertService } from '../../services/stock-alert.service';
import { StockWarning } from '../../models/stock.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
  warnings: StockWarning[] = [];
  loading = false;

  constructor(
    private stockAlertService: StockAlertService,
    private router: Router,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.stockAlertService.refreshLiveWarnings().subscribe({
      next: (data) => {
        // Full screen always shows every currently low stock item
        this.warnings = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Error loading notifications:', err);
        this.notificationService.showError('Failed to load notifications', 3000);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  goUpdateStock(): void {
    this.router.navigate(['/stock']);
  }
}

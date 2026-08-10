import { Component, OnInit, OnDestroy, HostListener, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { StockWarning } from '../../models/stock.models';
import { StockAlertService } from '../../services/stock-alert.service';
import { LoggerService } from '../../services/logger.service';
import { Subscription, Subject, interval } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  open = false;
  /** All live low-stock warnings (full list). */
  allWarnings: StockWarning[] = [];
  /** Bell panel list (session-dismissed filtered out). */
  warnings: StockWarning[] = [];
  loading = false;
  loadError = false;
  private readonly destroy$ = new Subject<void>();
  private refreshSub?: Subscription;

  constructor(
    private stockAlertService: StockAlertService,
    private router: Router,
    private logger: LoggerService,
    private host: ElementRef,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.reload(true);
    this.refreshSub = interval(60000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.reload(false));

    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.reload(false));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.refreshSub?.unsubscribe();
  }

  get unreadCount(): number {
    return this.warnings.length;
  }

  trackWarning(index: number, w: StockWarning): string | number {
    return w.stockItemId ?? w.id ?? `idx-${index}`;
  }

  toggle(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.open = !this.open;
    if (this.open) {
      this.reload(this.warnings.length === 0 && this.allWarnings.length === 0);
    }
    this.cdr.detectChanges();
  }

  /** Hide from bell only — does not clear the notifications screen / live low-stock list. */
  dismissFromBell(warning: StockWarning, event: MouseEvent): void {
    event.stopPropagation();
    if (warning.stockItemId == null) {
      return;
    }
    this.stockAlertService.dismissFromBell(warning.stockItemId);
    this.applyBellFilter();
    this.cdr.detectChanges();
  }

  showAll(): void {
    this.open = false;
    this.router.navigate(['/notifications']);
  }

  private reload(showLoading: boolean): void {
    if (!localStorage.getItem('accessToken')) {
      this.allWarnings = [];
      this.warnings = [];
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }
    if (showLoading && this.allWarnings.length === 0) {
      this.loading = true;
      this.cdr.detectChanges();
    }
    this.loadError = false;
    this.stockAlertService.refreshLiveWarnings().subscribe({
      next: (data) => {
        this.allWarnings = data || [];
        this.applyBellFilter();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.logger.error('Failed to load live stock alerts:', err);
        this.loadError = true;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private applyBellFilter(): void {
    this.warnings = this.stockAlertService.getBellVisibleWarnings();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.open) {
      return;
    }
    if (!this.host.nativeElement.contains(event.target)) {
      this.open = false;
      this.cdr.detectChanges();
    }
  }
}

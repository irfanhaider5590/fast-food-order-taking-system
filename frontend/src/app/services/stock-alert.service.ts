import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { StockService } from './stock.service';
import { StockItem, StockWarning } from '../models/stock.models';

const BELL_DISMISSED_KEY = 'stockBellDismissedIds';

/**
 * Live low-stock alerts based on current quantities (not acknowledge DB rows).
 * Bell "Dismiss" only hides items in this browser session until stock is restocked above threshold.
 */
@Injectable({ providedIn: 'root' })
export class StockAlertService {
  private readonly liveWarningsSubject = new BehaviorSubject<StockWarning[]>([]);
  readonly liveWarnings$ = this.liveWarningsSubject.asObservable();

  constructor(private stockService: StockService) {}

  getLiveWarningsSnapshot(): StockWarning[] {
    return this.liveWarningsSubject.value;
  }

  /** Refresh from stock items where qty <= threshold. */
  refreshLiveWarnings(): Observable<StockWarning[]> {
    return this.stockService.getStockItems().pipe(
      map((items) => this.toWarnings(items || [])),
      tap((warnings) => {
        this.pruneDismissed(warnings);
        this.liveWarningsSubject.next(warnings);
      })
    );
  }

  /** Warnings visible in the bell (excludes session-dismissed). */
  getBellVisibleWarnings(): StockWarning[] {
    const dismissed = this.getDismissedIds();
    return this.liveWarningsSubject.value.filter(
      (w) => w.stockItemId != null && !dismissed.has(w.stockItemId)
    );
  }

  dismissFromBell(stockItemId: number): void {
    const dismissed = this.getDismissedIds();
    dismissed.add(stockItemId);
    this.saveDismissedIds(dismissed);
  }

  clearBellDismiss(stockItemId: number): void {
    const dismissed = this.getDismissedIds();
    dismissed.delete(stockItemId);
    this.saveDismissedIds(dismissed);
  }

  private toWarnings(items: StockItem[]): StockWarning[] {
    return items
      .filter((item) => item.id != null && this.isLow(item))
      .map((item) => {
        const unit = item.unit || '';
        return {
          id: item.id,
          stockItemId: item.id!,
          stockItemNameEn: item.nameEn,
          stockItemNameUr: item.nameUr,
          warningMessageEn: `Low stock alert: ${item.nameEn} has only ${item.currentQuantity} ${unit} remaining (threshold: ${item.minThreshold} ${unit})`,
          warningMessageUr: item.nameUr
            ? `کم اسٹاک: ${item.nameUr} میں صرف ${item.currentQuantity} ${unit} باقی ہے (حد: ${item.minThreshold})`
            : '',
          currentQuantity: item.currentQuantity,
          thresholdQuantity: item.minThreshold,
          isAcknowledged: false
        } as StockWarning;
      });
  }

  private isLow(item: StockItem): boolean {
    if (item.isLowStock === true) {
      return true;
    }
    return Number(item.currentQuantity) <= Number(item.minThreshold);
  }

  /** Drop dismiss flags once item is no longer low (restocked). */
  private pruneDismissed(warnings: StockWarning[]): void {
    const lowIds = new Set(warnings.map((w) => w.stockItemId).filter((id): id is number => id != null));
    const dismissed = this.getDismissedIds();
    let changed = false;
    for (const id of [...dismissed]) {
      if (!lowIds.has(id)) {
        dismissed.delete(id);
        changed = true;
      }
    }
    if (changed) {
      this.saveDismissedIds(dismissed);
    }
  }

  private getDismissedIds(): Set<number> {
    try {
      const raw = sessionStorage.getItem(BELL_DISMISSED_KEY);
      if (!raw) {
        return new Set();
      }
      const arr = JSON.parse(raw) as number[];
      return new Set((arr || []).map(Number).filter((n) => !Number.isNaN(n)));
    } catch {
      return new Set();
    }
  }

  private saveDismissedIds(ids: Set<number>): void {
    sessionStorage.setItem(BELL_DISMISSED_KEY, JSON.stringify([...ids]));
  }
}

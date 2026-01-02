import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface Notification {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  private notificationId = 0;

  getNotifications(): Observable<Notification> {
    return this.notificationSubject.asObservable();
  }

  showSuccess(message: string, duration: number = 3000): void {
    this.showNotification({
      id: ++this.notificationId,
      message,
      type: 'success',
      duration
    });
  }

  showError(message: string, duration: number = 4000): void {
    this.showNotification({
      id: ++this.notificationId,
      message,
      type: 'error',
      duration
    });
  }

  showInfo(message: string, duration: number = 3000): void {
    this.showNotification({
      id: ++this.notificationId,
      message,
      type: 'info',
      duration
    });
  }

  showWarning(message: string, duration: number = 3500): void {
    this.showNotification({
      id: ++this.notificationId,
      message,
      type: 'warning',
      duration
    });
  }

  private showNotification(notification: Notification): void {
    this.notificationSubject.next(notification);
  }
}


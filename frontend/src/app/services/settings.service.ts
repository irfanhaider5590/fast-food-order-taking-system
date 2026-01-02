import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Settings, SettingsRequest } from '../models/settings.models';
import { LoggerService } from './logger.service';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl = `${environment.apiUrl}/settings`;

  constructor(
    private http: HttpClient,
    private logger: LoggerService
  ) {}

  getSettings(): Observable<Settings> {
    const token = localStorage.getItem('accessToken');
    this.logger.debug('SettingsService.getSettings() - Calling:', this.apiUrl);
    
    return this.http.get<Settings>(this.apiUrl, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).pipe(
      tap({
        next: (settings) => {
          this.logger.info('SettingsService.getSettings() - Success: Received settings');
          this.logger.debug('SettingsService.getSettings() - Settings data:', JSON.stringify(settings));
        },
        error: (error) => {
          this.logger.error('SettingsService.getSettings() - Error:', error);
        }
      })
    );
  }

  updateSettings(settingsData: SettingsRequest): Observable<Settings> {
    const token = localStorage.getItem('accessToken');
    this.logger.info('SettingsService.updateSettings() - Updating settings');
    
    return this.http.put<Settings>(this.apiUrl, settingsData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }
}


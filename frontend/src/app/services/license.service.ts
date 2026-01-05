import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LicenseStatus {
  isActivated: boolean;
  isValid: boolean;
  licenseType?: string;
  activatedAt?: string;
  expiresAt?: string;
  daysRemaining: number;
  message: string;
  machineId: string;
  shouldShowWarning?: boolean;
  warningMessage?: string;
}

export interface LicenseRequest {
  licenseKey: string;
}

@Injectable({
  providedIn: 'root'
})
export class LicenseService {
  private apiUrl = `${environment.apiUrl}/license`;

  constructor(private http: HttpClient) {}

  getLicenseStatus(): Observable<LicenseStatus> {
    return this.http.get<LicenseStatus>(`${this.apiUrl}/status`);
  }

  activateLicense(licenseKey: string): Observable<LicenseStatus> {
    return this.http.post<LicenseStatus>(`${this.apiUrl}/activate`, { licenseKey });
  }

  getMachineId(): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/machine-id`, { responseType: 'text' as 'json' });
  }
}


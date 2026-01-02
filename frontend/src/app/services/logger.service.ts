import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  info(message: string, ...args: any[]): void {
    console.log(`[INFO] ${message}`, ...args);
  }

  error(message: string, ...args: any[]): void {
    console.error(`[ERROR] ${message}`, ...args);
  }

  warn(message: string, ...args: any[]): void {
    console.warn(`[WARN] ${message}`, ...args);
  }

  debug(message: string, ...args: any[]): void {
    if (this.isDevelopmentMode()) {
      console.log(`[DEBUG] ${message}`, ...args);
    }
  }

  private isDevelopmentMode(): boolean {
    return !window.location.hostname.includes('localhost') === false;
  }
}


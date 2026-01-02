import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoggerService } from './logger.service';

export interface FileUploadResponse {
  url: string;
  path: string;
  filename: string;
}

@Injectable({
  providedIn: 'root'
})
export class FileUploadService {
  private apiUrl = `${environment.apiUrl}/files`;

  constructor(
    private http: HttpClient,
    private logger: LoggerService
  ) {}

  uploadImage(file: File, folder: string = 'general'): Observable<FileUploadResponse> {
    const token = localStorage.getItem('accessToken');
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);

    this.logger.debug('FileUploadService.uploadImage() - Uploading file:', file.name, 'to folder:', folder);

    return this.http.post<FileUploadResponse>(`${this.apiUrl}/upload`, formData, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).pipe(
      tap({
        next: (response) => {
          this.logger.info('FileUploadService.uploadImage() - Upload successful:', response);
        },
        error: (error) => {
          this.logger.error('FileUploadService.uploadImage() - Upload failed:', error);
        }
      })
    );
  }

  deleteImage(path: string): Observable<any> {
    const token = localStorage.getItem('accessToken');
    this.logger.debug('FileUploadService.deleteImage() - Deleting file:', path);

    return this.http.delete(`${this.apiUrl}/delete`, {
      params: { path },
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }
}

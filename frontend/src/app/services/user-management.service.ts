import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User, UserRequest, Role, Branch } from '../models/user.models';
import { LoggerService } from './logger.service';

@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private apiUrl = `${environment.apiUrl}/admin/users`;

  constructor(
    private http: HttpClient,
    private logger: LoggerService
  ) {}

  getUsers(): Observable<User[]> {
    const token = localStorage.getItem('accessToken');
    this.logger.debug('UserManagementService.getUsers() - Calling:', this.apiUrl);
    
    return this.http.get<User[]>(this.apiUrl, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }).pipe(
      // tap({
      //   next: (users) => this.logger.info(`UserManagementService.getUsers() - Success: Received ${users.length} users`),
      //   error: (error) => this.logger.error('UserManagementService.getUsers() - Error:', error)
      // })
    );
  }

  getUserById(id: number): Observable<User> {
    const token = localStorage.getItem('accessToken');
    return this.http.get<User>(`${this.apiUrl}/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  createUser(userData: UserRequest): Observable<User> {
    const token = localStorage.getItem('accessToken');
    this.logger.info('UserManagementService.createUser() - Creating user:', userData.username);
    
    return this.http.post<User>(this.apiUrl, userData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  updateUser(id: number, userData: UserRequest): Observable<User> {
    const token = localStorage.getItem('accessToken');
    this.logger.info(`UserManagementService.updateUser() - Updating user ${id}`);
    
    return this.http.put<User>(`${this.apiUrl}/${id}`, userData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  deleteUser(id: number): Observable<void> {
    const token = localStorage.getItem('accessToken');
    this.logger.info(`UserManagementService.deleteUser() - Deleting user ${id}`);
    
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  toggleUserStatus(id: number): Observable<User> {
    const token = localStorage.getItem('accessToken');
    this.logger.info(`UserManagementService.toggleUserStatus() - Toggling status for user ${id}`);
    
    return this.http.patch<User>(`${this.apiUrl}/${id}/toggle-status`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  getRoles(): Observable<Role[]> {
    // This would need a roles endpoint - for now return mock data
    return new Observable(observer => {
      observer.next([
        { id: 1, name: 'ADMIN', description: 'Administrator' },
        { id: 2, name: 'BRANCH_MANAGER', description: 'Branch Manager' },
        { id: 3, name: 'STAFF', description: 'Staff Member' }
      ]);
      observer.complete();
    });
  }

  getBranches(): Observable<Branch[]> {
    // This would need a branches endpoint - for now return mock data
    return new Observable(observer => {
      observer.next([
        { id: 1, name: 'Main Branch', address: '123 Main St' }
      ]);
      observer.complete();
    });
  }
}


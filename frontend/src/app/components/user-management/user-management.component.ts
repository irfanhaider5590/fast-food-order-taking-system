import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserManagementService } from '../../services/user-management.service';
import { User, UserRequest, Role, Branch } from '../../models/user.models';
import { LoggerService } from '../../services/logger.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  roles: Role[] = [];
  branches: Branch[] = [];
  loading = false;
  error: string | null = null;
  
  showUserForm = false;
  editingUser: User | null = null;
  userForm: UserRequest = {
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
    roleId: 0,
    branchId: undefined,
    isActive: true
  };

  constructor(
    private userManagementService: UserManagementService,
    private logger: LoggerService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadRoles();
    this.loadBranches();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.logger.debug('UserManagementComponent.loadUsers() - Starting to load users');
    this.userManagementService.getUsers().subscribe({
      next: (users) => {
        this.logger.debug('UserManagementComponent.loadUsers() - Received users:', JSON.stringify(users));
        this.logger.debug('UserManagementComponent.loadUsers() - Users array length:', users?.length || 0);
        this.users = users || [];
        this.loading = false;
        this.logger.info(`Loaded ${this.users.length} users`);
        this.logger.debug('UserManagementComponent.loadUsers() - Users after assignment:', JSON.stringify(this.users));
        this.cdr.detectChanges(); // Force change detection
      },
      error: (err) => {
        this.error = 'Failed to load users';
        this.loading = false;
        this.logger.error('Error loading users:', err);
        this.notificationService.showError('Failed to load users', 3000);
        this.cdr.detectChanges();
      }
    });
  }

  loadRoles(): void {
    this.userManagementService.getRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
      },
      error: (err) => {
        this.logger.error('Error loading roles:', err);
      }
    });
  }

  loadBranches(): void {
    this.userManagementService.getBranches().subscribe({
      next: (branches) => {
        this.branches = branches;
      },
      error: (err) => {
        this.logger.error('Error loading branches:', err);
      }
    });
  }

  openAddUserForm(): void {
    this.editingUser = null;
    this.userForm = {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phone: '',
      roleId: 0,
      branchId: undefined,
      isActive: true
    };
    this.showUserForm = true;
  }

  openEditForm(user: User): void {
    this.editingUser = user;
    this.userForm = {
      username: user.username,
      email: user.email,
      password: '', // Don't pre-fill password
      fullName: user.fullName,
      phone: user.phone || '',
      roleId: user.roleId || 0,
      branchId: user.branchId,
      isActive: user.isActive
    };
    this.showUserForm = true;
  }

  closeForm(): void {
    this.showUserForm = false;
    this.editingUser = null;
  }

  saveUser(): void {
    if (!this.userForm.username || !this.userForm.email || !this.userForm.fullName || !this.userForm.roleId) {
      this.notificationService.showWarning('Please fill all required fields', 3000);
      return;
    }

    if (!this.editingUser && !this.userForm.password) {
      this.notificationService.showWarning('Password is required for new users', 3000);
      return;
    }

    const userData: UserRequest = { ...this.userForm };
    if (this.editingUser && !userData.password) {
      delete userData.password; // Don't send password if not changed
    }

    if (this.editingUser) {
      this.userManagementService.updateUser(this.editingUser.id!, userData).subscribe({
        next: () => {
          this.notificationService.showSuccess('User updated successfully', 3000);
          this.closeForm();
          this.loadUsers();
        },
        error: (err) => {
          this.logger.error('Error updating user:', err);
          this.notificationService.showError(
            'Failed to update user: ' + (err.error?.message || 'Unknown error'),
            4000
          );
        }
      });
    } else {
      this.userManagementService.createUser(userData).subscribe({
        next: () => {
          this.notificationService.showSuccess('User created successfully', 3000);
          this.closeForm();
          this.loadUsers();
        },
        error: (err) => {
          this.logger.error('Error creating user:', err);
          this.notificationService.showError(
            'Failed to create user: ' + (err.error?.message || 'Unknown error'),
            4000
          );
        }
      });
    }
  }

  deleteUser(user: User): void {
    if (!confirm(`Are you sure you want to delete user "${user.username}"?`)) {
      return;
    }

    this.userManagementService.deleteUser(user.id!).subscribe({
      next: () => {
        this.notificationService.showSuccess('User deleted successfully', 3000);
        this.loadUsers();
      },
      error: (err) => {
        this.logger.error('Error deleting user:', err);
        this.notificationService.showError(
          'Failed to delete user: ' + (err.error?.message || 'Unknown error'),
          4000
        );
      }
    });
  }

  toggleUserStatus(user: User): void {
    this.userManagementService.toggleUserStatus(user.id!).subscribe({
      next: () => {
        this.notificationService.showSuccess(
          `User ${user.isActive ? 'deactivated' : 'activated'} successfully`,
          3000
        );
        this.loadUsers();
      },
      error: (err) => {
        this.logger.error('Error toggling user status:', err);
        this.notificationService.showError(
          'Failed to update user status: ' + (err.error?.message || 'Unknown error'),
          4000
        );
      }
    });
  }

  getRoleName(roleId: number): string {
    const role = this.roles.find(r => r.id === roleId);
    return role ? role.name : 'Unknown';
  }
}


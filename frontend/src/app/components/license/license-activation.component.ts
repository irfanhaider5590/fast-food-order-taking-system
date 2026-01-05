import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LicenseService, LicenseStatus } from '../../services/license.service';
import { NotificationService } from '../../services/notification.service';
import { LoggerService } from '../../services/logger.service';

@Component({
  selector: 'app-license-activation',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './license-activation.component.html',
  styleUrls: ['./license-activation.component.css']
})
export class LicenseActivationComponent implements OnInit {
  licenseForm: FormGroup;
  licenseStatus: LicenseStatus | null = null;
  loading = false;
  machineId: string = '';

  constructor(
    private fb: FormBuilder,
    private licenseService: LicenseService,
    private notificationService: NotificationService,
    private logger: LoggerService
  ) {
    this.licenseForm = this.fb.group({
      licenseKey: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadLicenseStatus();
    this.loadMachineId();
  }

  loadLicenseStatus(): void {
    this.loading = true;
    this.licenseService.getLicenseStatus().subscribe({
      next: (status) => {
        this.licenseStatus = status;
        this.loading = false;
        
        if (!status.isValid && status.isActivated) {
          this.notificationService.showWarning(status.message, 10000);
        }
      },
      error: (err) => {
        this.logger.error('Error loading license status:', err);
        this.loading = false;
      }
    });
  }

  loadMachineId(): void {
    this.licenseService.getMachineId().subscribe({
      next: (id) => {
        this.machineId = id;
      },
      error: (err) => {
        this.logger.error('Error loading machine ID:', err);
      }
    });
  }

  activateLicense(): void {
    if (this.licenseForm.invalid) {
      return;
    }

    const licenseKey = this.licenseForm.get('licenseKey')?.value;
    this.loading = true;

    this.licenseService.activateLicense(licenseKey).subscribe({
      next: (status) => {
        this.licenseStatus = status;
        this.loading = false;
        
        if (status.isValid) {
          this.notificationService.showSuccess('License activated successfully!', 5000);
          this.licenseForm.reset();
        } else {
          this.notificationService.showError(status.message, 8000);
        }
      },
      error: (err) => {
        this.logger.error('Error activating license:', err);
        const errorMessage = err.error?.message || 'Failed to activate license';
        this.notificationService.showError(errorMessage, 8000);
        this.loading = false;
      }
    });
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  }
}


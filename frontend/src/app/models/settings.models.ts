export interface Settings {
  id?: number;
  brandName: string;
  brandLogoUrl?: string;
  contactPhone?: string;
  contactEmail?: string;
  address?: string;
  createdAt?: string;
  updatedAt?: string;
  createdByUsername?: string;
  updatedByUsername?: string;
}

export interface SettingsRequest {
  brandName: string;
  brandLogoUrl?: string;
  contactPhone?: string;
  contactEmail?: string;
  address?: string;
}


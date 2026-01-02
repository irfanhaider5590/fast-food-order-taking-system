export interface User {
  id?: number;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  roleId: number;
  roleName?: string;
  branchId?: number;
  branchName?: string;
  isActive: boolean;
  lastLogin?: string;
  createdAt?: string;
  updatedAt?: string;
  createdByUsername?: string;
  updatedByUsername?: string;
}

export interface UserRequest {
  username: string;
  email: string;
  password?: string;
  fullName: string;
  phone?: string;
  roleId: number;
  branchId?: number;
  isActive?: boolean;
}

export interface Role {
  id: number;
  name: string;
  description?: string;
}

export interface Branch {
  id: number;
  name: string;
  address?: string;
}


import api from '@/lib/axios';
import {
  AddressRequest,
  AddressResponse,
  ApiResponse,
  AuthResponse,
  Page,
  UserResponse,
  UserRole,
} from '@/types';

export const identityService = {
  // Auth
  async login(email: string, password: string): Promise<AuthResponse> {
    const res = await api.post<ApiResponse<AuthResponse>>('/api/identity/auth/login', {
      email,
      password,
    });
    return res.data.data;
  },

  async register(email: string, password: string, fullName: string): Promise<UserResponse> {
    const res = await api.post<ApiResponse<UserResponse>>('/api/identity/auth/register', {
      email,
      password,
      fullName,
    });
    return res.data.data;
  },

  async forgotPassword(email: string): Promise<void> {
    await api.post('/api/identity/auth/forgot-password', { email });
  },

  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post('/api/identity/auth/reset-password', { token, newPassword });
  },

  async verifyEmail(token: string): Promise<void> {
    await api.get(`/api/identity/auth/verify-email?token=${encodeURIComponent(token)}`);
  },

  async loginGoogle(idToken: string): Promise<AuthResponse> {
    const res = await api.post<ApiResponse<AuthResponse>>('/api/identity/auth/google', { idToken });
    return res.data.data;
  },

  // Profile
  async getProfile(): Promise<UserResponse> {
    const res = await api.get<ApiResponse<UserResponse>>('/api/identity/users/me');
    return res.data.data;
  },

  async updateProfile(data: {
    fullName?: string;
    phoneNumber?: string;
    dateOfBirth?: string;
    avatarUrl?: string;
  }): Promise<UserResponse> {
    const res = await api.put<ApiResponse<UserResponse>>('/api/identity/users/me', data);
    return res.data.data;
  },

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await api.put('/api/identity/users/me/password', { oldPassword, newPassword });
  },

  // Addresses
  async getAddresses(): Promise<AddressResponse[]> {
    const res = await api.get<ApiResponse<AddressResponse[]>>('/api/identity/users/me/addresses');
    return res.data.data || [];
  },

  async createAddress(data: AddressRequest): Promise<AddressResponse> {
    const res = await api.post<ApiResponse<AddressResponse>>('/api/identity/users/me/addresses', data);
    return res.data.data;
  },

  async updateAddress(id: number, data: AddressRequest): Promise<AddressResponse> {
    const res = await api.put<ApiResponse<AddressResponse>>(
      `/api/identity/users/me/addresses/${id}`,
      data
    );
    return res.data.data;
  },

  async setDefaultAddress(id: number): Promise<AddressResponse> {
    const res = await api.put<ApiResponse<AddressResponse>>(
      `/api/identity/users/me/addresses/${id}/default`
    );
    return res.data.data;
  },

  async deleteAddress(id: number): Promise<void> {
    await api.delete(`/api/identity/users/me/addresses/${id}`);
  },

  // ADMIN APIs
  async getAdminUsers(params?: {
    keyword?: string;
    role?: string;
    isActive?: boolean;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: string;
  }): Promise<Page<UserResponse>> {
    const res = await api.get<ApiResponse<Page<UserResponse>>>('/api/identity/admin/users', { params });
    return res.data.data;
  },

  async getAdminUserById(id: number): Promise<UserResponse> {
    const res = await api.get<ApiResponse<UserResponse>>(`/api/identity/admin/users/${id}`);
    return res.data.data;
  },

  async updateUserRole(id: number, role: UserRole): Promise<UserResponse> {
    const res = await api.put<ApiResponse<UserResponse>>(`/api/identity/admin/users/${id}/role`, {
      role,
    });
    return res.data.data;
  },

  async updateUserStatus(id: number, isActive: boolean): Promise<UserResponse> {
    const res = await api.put<ApiResponse<UserResponse>>(`/api/identity/admin/users/${id}/status`, {
      isActive,
    });
    return res.data.data;
  },
};

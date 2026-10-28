import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { AuthResponse, UserResponse, UserRole } from '@/types';
import api from '@/lib/axios';
import { useCartStore } from './useCartStore';
import { useWishlistStore } from './useWishlistStore';

interface AuthState {
  user: UserResponse | null;
  accessToken: string | null;
  refreshToken: string | null;
  role: UserRole | null;
  isAuthenticated: boolean;
  login: (authData: AuthResponse) => void;
  logout: () => Promise<void>;
  updateUser: (user: Partial<UserResponse>) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  fetchProfile: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      role: null,
      isAuthenticated: false,

      login: (authData: AuthResponse) => {
        set({
          accessToken: authData.accessToken,
          refreshToken: authData.refreshToken,
          role: authData.role,
          isAuthenticated: true,
          user: {
            id: authData.userId,
            email: authData.email,
            fullName: authData.fullName,
            role: authData.role,
            provider: 'LOCAL',
            isEmailVerified: true,
            isActive: true,
            createdAt: new Date().toISOString(),
          },
        });
        // Fetch full profile in the background
        get().fetchProfile();
        // Sync local offline items to server Redis cart & fetch wishlist
        useCartStore.getState().syncLocalCartToRemote();
        useWishlistStore.getState().fetchWishlist();
      },

      logout: async () => {
        const refreshToken = get().refreshToken;
        try {
          if (refreshToken) {
            await api.post('/api/identity/auth/logout', { refreshToken });
          }
        } catch (e) {
          console.warn('Logout API call failed', e);
        } finally {
          useCartStore.getState().closeCart();
          useCartStore.setState({ items: [] });
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            role: null,
            isAuthenticated: false,
          });
        }
      },

      updateUser: (updatedFields) => {
        set((state) => ({
          user: state.user ? { ...state.user, ...updatedFields } : null,
        }));
      },

      setTokens: (accessToken: string, refreshToken: string) => {
        set({ accessToken, refreshToken });
      },

      fetchProfile: async () => {
        try {
          const res = await api.get('/api/identity/users/me');
          if (res.data?.success && res.data?.data) {
            set({
              user: res.data.data,
              role: res.data.data.role,
            });
          }
        } catch (error) {
          console.error('Fetch profile failed:', error);
        }
      },
    }),
    {
      name: 'moc-huong-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        role: state.role,
        isAuthenticated: state.isAuthenticated,
        user: state.user,
      }),
    }
  )
);

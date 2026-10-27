import { create } from 'zustand';
import api from '@/lib/axios';
import { useAuthStore } from './useAuthStore';
import toast from 'react-hot-toast';

interface WishlistState {
  wishlistIds: number[];
  isLoading: boolean;
  fetchWishlist: () => Promise<void>;
  toggleWishlist: (productId: number) => Promise<boolean>;
  isInWishlist: (productId: number) => boolean;
}

export const useWishlistStore = create<WishlistState>((set, get) => ({
  wishlistIds: [],
  isLoading: false,

  fetchWishlist: async () => {
    const { isAuthenticated } = useAuthStore.getState();
    if (!isAuthenticated) return;

    try {
      set({ isLoading: true });
      const res = await api.get('/api/identity/users/me/wishlists');
      if (res.data?.success && Array.isArray(res.data.data)) {
        const ids = res.data.data.map((w: any) => w.productId || w.id);
        set({ wishlistIds: ids });
      }
    } catch (e) {
      console.warn('Failed to fetch wishlist', e);
    } finally {
      set({ isLoading: false });
    }
  },

  toggleWishlist: async (productId: number) => {
    const { isAuthenticated } = useAuthStore.getState();
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để lưu vào danh sách yêu thích');
      return false;
    }

    const current = get().wishlistIds;
    const isSaved = current.includes(productId);

    try {
      if (isSaved) {
        set({ wishlistIds: current.filter((id) => id !== productId) });
        await api.delete(`/api/identity/users/me/wishlists/${productId}`);
        toast.success('Đã bỏ khỏi danh sách yêu thích');
        return false;
      } else {
        set({ wishlistIds: [...current, productId] });
        await api.post(`/api/identity/users/me/wishlists/${productId}`);
        toast.success('Đã thêm vào danh sách yêu thích', {
          icon: '❤️',
          style: {
            background: '#F2ECE1',
            color: '#231C18',
            border: '1px solid #C89B57',
          },
        });
        return true;
      }
    } catch (err: any) {
      // Revert on error
      set({ wishlistIds: current });
      toast.error('Có lỗi xảy ra khi cập nhật yêu thích');
      return isSaved;
    }
  },

  isInWishlist: (productId: number) => {
    return get().wishlistIds.includes(productId);
  },
}));

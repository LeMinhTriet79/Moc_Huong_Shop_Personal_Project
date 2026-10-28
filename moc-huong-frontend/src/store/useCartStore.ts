import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { CartItem } from '@/types';
import api from '@/lib/axios';
import { useAuthStore } from './useAuthStore';
import toast from 'react-hot-toast';

interface CartState {
  items: CartItem[];
  isOpen: boolean;
  isLoading: boolean;
  openCart: () => void;
  closeCart: () => void;
  toggleCart: () => void;
  fetchCart: () => Promise<void>;
  syncLocalCartToRemote: () => Promise<void>;
  addItem: (item: CartItem) => Promise<void>;
  updateQuantity: (productId: number, variantId: number, quantity: number) => Promise<void>;
  removeItem: (productId: number, variantId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  getTotalItems: () => number;
  getSubtotal: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      isOpen: false,
      isLoading: false,

      openCart: () => set({ isOpen: true }),
      closeCart: () => set({ isOpen: false }),
      toggleCart: () => set((state) => ({ isOpen: !state.isOpen })),

      fetchCart: async () => {
        const { isAuthenticated } = useAuthStore.getState();
        if (!isAuthenticated) return;

        try {
          set({ isLoading: true });
          const res = await api.get('/api/order/cart');
          if (res.data?.success && Array.isArray(res.data.data)) {
            set({ items: res.data.data });
          }
        } catch (error) {
          console.warn('Failed to fetch remote cart:', error);
        } finally {
          set({ isLoading: false });
        }
      },

      syncLocalCartToRemote: async () => {
        const { isAuthenticated } = useAuthStore.getState();
        if (!isAuthenticated) return;

        const localItems = get().items;
        if (localItems.length > 0) {
          for (const item of localItems) {
            try {
              await api.post('/api/order/cart/add', {
                productId: item.productId,
                variantId: item.variantId,
                productName: item.productName,
                variantName: item.variantName,
                productImageUrl: item.productImageUrl,
                price: item.price,
                quantity: item.quantity,
              });
            } catch (err) {
              console.warn(`Sync item ${item.productId}-${item.variantId} failed`, err);
            }
          }
        }
        await get().fetchCart();
      },

      addItem: async (newItem: CartItem) => {
        const { isAuthenticated } = useAuthStore.getState();
        const currentItems = [...get().items];
        const existingIndex = currentItems.findIndex(
          (i) => i.productId === newItem.productId && i.variantId === newItem.variantId
        );

        if (existingIndex > -1) {
          currentItems[existingIndex].quantity += newItem.quantity;
        } else {
          currentItems.push(newItem);
        }
        set({ items: currentItems });

        if (isAuthenticated) {
          try {
            await api.post('/api/order/cart/add', {
              productId: newItem.productId,
              variantId: newItem.variantId,
              productName: newItem.productName,
              variantName: newItem.variantName,
              productImageUrl: newItem.productImageUrl,
              price: newItem.price,
              quantity: newItem.quantity,
            });
          } catch (error: any) {
            console.error('Add to cart API failed:', error);
            const msg = error.response?.data?.message || 'Không thể thêm vào giỏ hàng';
            toast.error(msg);
            get().fetchCart();
            return;
          }
        }

        toast.success(`Đã thêm ${newItem.productName} vào giỏ`, {
          icon: '🌿',
          style: {
            background: '#F2ECE1',
            color: '#231C18',
            border: '1px solid #C89B57',
          },
        });
        set({ isOpen: true });
      },

      updateQuantity: async (productId: number, variantId: number, quantity: number) => {
        if (quantity <= 0) {
          await get().removeItem(productId, variantId);
          return;
        }

        const currentItems = get().items.map((item) =>
          item.productId === productId && item.variantId === variantId ? { ...item, quantity } : item
        );
        set({ items: currentItems });

        const { isAuthenticated } = useAuthStore.getState();
        if (isAuthenticated) {
          try {
            await api.put('/api/order/cart/update', {
              productId,
              variantId,
              quantity,
            });
          } catch (error) {
            console.error('Update cart item quantity failed:', error);
            get().fetchCart();
          }
        }
      },

      removeItem: async (productId: number, variantId: number) => {
        const filtered = get().items.filter(
          (item) => !(item.productId === productId && item.variantId === variantId)
        );
        set({ items: filtered });

        const { isAuthenticated } = useAuthStore.getState();
        if (isAuthenticated) {
          try {
            await api.delete(`/api/order/cart/remove/${productId}/${variantId}`);
          } catch (error) {
            console.error('Remove cart item failed:', error);
            get().fetchCart();
          }
        }
      },

      clearCart: async () => {
        set({ items: [] });
        const { isAuthenticated } = useAuthStore.getState();
        if (isAuthenticated) {
          try {
            await api.delete('/api/order/cart/clear');
          } catch (error) {
            console.error('Clear cart failed:', error);
          }
        }
      },

      getTotalItems: () => {
        return get().items.reduce((total, item) => total + item.quantity, 0);
      },

      getSubtotal: () => {
        return get().items.reduce((total, item) => total + item.price * item.quantity, 0);
      },
    }),
    {
      name: 'moc-huong-cart',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ items: state.items }),
    }
  )
);

'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import api from '@/lib/axios';
import { ProductResponse } from '@/types';
import ProductCard from '@/components/product/ProductCard';
import { useWishlistStore } from '@/store/useWishlistStore';

export default function WishlistPage() {
  const { fetchWishlist } = useWishlistStore();
  const [favoriteProducts, setFavoriteProducts] = useState<ProductResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadFavorites() {
      setIsLoading(true);
      await fetchWishlist();
      try {
        const res = await api.get('/api/identity/users/me/wishlists');
        const list = res.data?.data || [];

        // For each favorite item, fetch product details or map if product is embedded
        const prods: ProductResponse[] = [];
        for (const item of list) {
          if (item.product) {
            prods.push(item.product);
          } else if (item.productId) {
            try {
              const pRes = await api.get(`/api/catalog/products/${item.productId}`);
              if (pRes.data?.data) prods.push(pRes.data.data);
            } catch (e) {
              console.warn('Could not load favorite product details:', item.productId);
            }
          }
        }
        setFavoriteProducts(prods);
      } catch (err) {
        console.error('Failed to load favorites:', err);
      } finally {
        setIsLoading(false);
      }
    }
    loadFavorites();
  }, [fetchWishlist]);

  return (
    <div className="space-y-6">
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="font-serif text-2xl font-bold text-primary">Danh Sách Yêu Thích</h2>
          <p className="text-xs text-text-wood-muted mt-0.5">
            Những vật phẩm trầm hương thanh tịnh bạn đã lưu lại để chiêm ngưỡng
          </p>
        </div>

        <span className="px-4 py-1.5 rounded-full bg-secondary-fixed text-primary text-xs font-bold self-start sm:self-auto">
          {favoriteProducts.length} vật phẩm đã lưu
        </span>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="h-80 rounded-2xl bg-bg-warm-sand animate-pulse border border-border-bamboo" />
          ))}
        </div>
      ) : favoriteProducts.length === 0 ? (
        <div className="bg-bg-warm-sand/50 rounded-3xl p-12 text-center border border-dashed border-border-bamboo flex flex-col items-center">
          <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-text-wood-muted mb-4 border border-border-bamboo">
            <span className="material-symbols-outlined text-[32px]">favorite_border</span>
          </div>
          <h3 className="font-serif text-xl font-bold text-primary mb-2">Danh sách yêu thích đang trống</h3>
          <p className="text-xs text-text-wood-muted max-w-sm mb-6">
            Khám phá những phẩm hương tự nhiên và bấm biểu tượng trái tim để lưu lại vào đây.
          </p>
          <Link
            href="/shop"
            className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm"
          >
            Dạo Xem Cửa Hàng
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {favoriteProducts.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </div>
  );
}

'use client';

import React from 'react';
import Link from 'next/link';
import { ProductResponse } from '@/types';
import { formatCurrency } from '@/lib/utils';
import { useWishlistStore } from '@/store/useWishlistStore';
import { useCartStore } from '@/store/useCartStore';

import { catalogService } from '@/services/catalogService';

interface ProductCardProps {
  product: ProductResponse;
}

export default function ProductCard({ product }: ProductCardProps) {
  const { isInWishlist, toggleWishlist } = useWishlistStore();
  const { addItem } = useCartStore();

  const isFav = isInWishlist(product.id);
  const primaryImg =
    product.images?.find((img) => img.isPrimary)?.imageUrl ||
    product.images?.[0]?.imageUrl ||
    (product as any).primaryImageUrl ||
    'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=500';

  const displayPrice =
    product.basePrice ||
    product.startingPrice ||
    (product.variants && product.variants.length > 0
      ? Math.min(...product.variants.map((v) => v.price))
      : 0);

  const handleQuickAdd = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    let variant = product.variants?.[0];
    if (!variant) {
      try {
        const detail = await catalogService.getProductById(product.slug || product.id);
        variant = detail?.variants?.[0];
      } catch (err) {
        console.error('Failed to fetch product detail for variant', err);
      }
    }
    if (!variant) return;

    await addItem({
      productId: product.id,
      variantId: variant.id,
      sku: variant.sku,
      productName: product.name,
      variantName: variant.variantName,
      productImageUrl: primaryImg,
      price: variant.price,
      quantity: 1,
    });
  };

  const handleToggleFav = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    await toggleWishlist(product.id);
  };

  return (
    <div className="group bg-bg-warm-sand rounded-2xl overflow-hidden border border-border-bamboo/60 hover:border-secondary transition-all duration-300 hover:shadow-xl flex flex-col justify-between">
      {/* Top Image Frame */}
      <Link href={`/products/${product.id}`} className="relative block overflow-hidden aspect-square bg-surface-container">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={primaryImg}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 ease-out"
        />

        {/* Tag Pill */}
        <div className="absolute top-3 left-3 flex flex-col gap-1">
          {product.tags && (
            <span className="bg-primary/90 text-secondary-fixed text-[11px] font-caption-meta uppercase tracking-wider px-2.5 py-1 rounded-full backdrop-blur-xs font-semibold">
              {Array.isArray(product.tags) ? product.tags[0] : String(product.tags).split(',')[0]}
            </span>
          )}
        </div>

        {/* Wishlist Button */}
        <button
          onClick={handleToggleFav}
          aria-label="Lưu yêu thích"
          className={`absolute top-3 right-3 w-9 h-9 rounded-full bg-surface/85 backdrop-blur-md flex items-center justify-center transition-all ${
            isFav
              ? 'text-status-cinnabar scale-110 shadow-sm'
              : 'text-text-wood-muted hover:text-status-cinnabar hover:scale-105'
          }`}
        >
          <span
            className="material-symbols-outlined text-[20px]"
            style={{ fontVariationSettings: isFav ? "'FILL' 1" : "'FILL' 0" }}
          >
            favorite
          </span>
        </button>
      </Link>

      {/* Body Content */}
      <div className="p-5 flex-1 flex flex-col justify-between">
        <div>
          {/* Brand & Rating */}
          <div className="flex items-center justify-between text-xs text-text-wood-muted mb-1.5">
            <span className="font-caption-meta uppercase tracking-wider text-[11px]">
              {product.brand || 'MỘC HƯƠNG'}
            </span>
            <div className="flex items-center gap-1 text-secondary">
              <span className="material-symbols-outlined text-[14px] fill-current">star</span>
              <span className="font-semibold text-text-ink-black text-xs">
                {product.averageRating ? product.averageRating.toFixed(1) : '5.0'}
              </span>
              <span className="text-[11px] text-text-wood-muted">
                ({product.reviewCount || 12})
              </span>
            </div>
          </div>

          {/* Product Name */}
          <Link href={`/products/${product.id}`} className="block">
            <h3 className="font-serif font-bold text-base text-text-ink-black group-hover:text-primary transition-colors line-clamp-2 leading-snug">
              {product.name}
            </h3>
          </Link>
        </div>

        {/* Price & Action Row */}
        <div className="pt-4 mt-2 border-t border-border-bamboo/40 flex items-center justify-between gap-2">
          <div className="flex flex-col">
            <span className="font-caption-meta text-[11px] text-text-wood-muted uppercase">Giá từ</span>
            <span className="font-mono text-base font-bold text-primary">
              {formatCurrency(displayPrice)}
            </span>
          </div>

          <button
            onClick={handleQuickAdd}
            className="px-3.5 py-2 rounded-xl bg-primary text-secondary-fixed font-label-md text-xs font-semibold hover:bg-primary-container transition-all flex items-center gap-1.5 shadow-sm active:scale-95"
            title="Thêm nhanh vào giỏ"
          >
            <span className="material-symbols-outlined text-[16px]">add_shopping_cart</span>
            <span className="hidden sm:inline">Chọn Mua</span>
          </button>
        </div>
      </div>
    </div>
  );
}

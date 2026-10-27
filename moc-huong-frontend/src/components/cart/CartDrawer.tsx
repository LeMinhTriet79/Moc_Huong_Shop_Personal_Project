'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import { useCartStore } from '@/store/useCartStore';
import { formatCurrency } from '@/lib/utils';

export default function CartDrawer() {
  const { items, isOpen, closeCart, updateQuantity, removeItem, getSubtotal, getTotalItems, fetchCart } =
    useCartStore();

  useEffect(() => {
    if (isOpen) {
      fetchCart();
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, fetchCart]);

  if (!isOpen) return null;

  const subtotal = getSubtotal();
  const totalItems = getTotalItems();
  const freeShippingThreshold = 500000;
  const isFreeShipping = subtotal >= freeShippingThreshold;
  const progressPercent = Math.min(100, Math.round((subtotal / freeShippingThreshold) * 100));
  const remainingForFreeShipping = Math.max(0, freeShippingThreshold - subtotal);

  return (
    <div className="fixed inset-0 z-50 overflow-hidden animate-in fade-in duration-200">
      {/* Backdrop */}
      <div
        onClick={closeCart}
        className="absolute inset-0 bg-bg-agarwood-dark/60 backdrop-blur-sm transition-opacity"
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-surface shadow-2xl flex flex-col border-l border-border-bamboo animate-in slide-in-from-right duration-300">
          {/* Drawer Header */}
          <div className="p-6 border-b border-border-bamboo/60 flex items-center justify-between bg-bg-warm-sand/50">
            <div className="flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[24px]">shopping_bag</span>
              <h2 className="font-headline-sm text-headline-sm text-primary font-serif font-bold">
                Giỏ Hàng
              </h2>
              <span className="font-caption-meta bg-primary text-secondary-fixed px-2 py-0.5 rounded-full text-xs font-semibold">
                {totalItems} món
              </span>
            </div>
            <button
              onClick={closeCart}
              className="p-1.5 rounded-lg text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
              aria-label="Đóng giỏ hàng"
            >
              <span className="material-symbols-outlined text-[24px]">close</span>
            </button>
          </div>

          {/* Free Shipping Meter */}
          <div className="p-4 bg-surface-container-low border-b border-border-bamboo/40">
            <div className="flex items-center justify-between text-xs mb-1.5">
              <span className="font-medium text-text-ink-black flex items-center gap-1">
                <span className="material-symbols-outlined text-[16px] text-tertiary">local_shipping</span>
                {isFreeShipping ? (
                  <span className="text-accent-lotus-jade font-semibold">Đã đạt Miễn phí vận chuyển toàn quốc!</span>
                ) : (
                  <span>
                    Mua thêm{' '}
                    <strong className="text-status-cinnabar">{formatCurrency(remainingForFreeShipping)}</strong> để
                    được miễn phí vận chuyển
                  </span>
                )}
              </span>
              <span className="font-mono text-text-wood-muted">{progressPercent}%</span>
            </div>
            <div className="w-full h-2 bg-surface-container-high rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  isFreeShipping ? 'bg-accent-lotus-jade' : 'bg-primary'
                }`}
                style={{ width: `${progressPercent}%` }}
              />
            </div>
          </div>

          {/* Cart Items List */}
          <div className="flex-1 overflow-y-auto p-6 divide-y divide-border-bamboo/40 space-y-4">
            {items.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center py-12">
                <div className="w-20 h-20 rounded-full bg-bg-warm-sand flex items-center justify-center text-text-wood-muted mb-4 border border-border-bamboo/60">
                  <span className="material-symbols-outlined text-[36px]">spa</span>
                </div>
                <h3 className="font-headline-sm text-primary mb-2 font-serif">Giỏ hàng còn thanh tịnh</h3>
                <p className="font-body-sm text-text-wood-muted max-w-xs mb-6">
                  Chưa có phẩm hương hay vật phẩm thiền định nào trong giỏ của bạn.
                </p>
                <button
                  onClick={closeCart}
                  className="px-6 py-2.5 rounded-lg bg-primary text-secondary-fixed font-label-md font-semibold hover:bg-primary-container transition-colors shadow-sm"
                >
                  Khám Phá Cửa Hàng
                </button>
              </div>
            ) : (
              items.map((item) => (
                <div key={`${item.productId}-${item.variantId}`} className="pt-4 first:pt-0 flex gap-4">
                  {/* Thumbnail */}
                  <div className="w-20 h-20 rounded-lg overflow-hidden bg-bg-warm-sand border border-border-bamboo/50 shrink-0">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={item.productImageUrl || 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=300'}
                      alt={item.productName}
                      className="w-full h-full object-cover"
                    />
                  </div>

                  {/* Info & Controls */}
                  <div className="flex-1 flex flex-col justify-between">
                    <div>
                      <div className="flex items-start justify-between gap-2">
                        <h4 className="font-label-lg font-semibold text-text-ink-black line-clamp-1">
                          {item.productName}
                        </h4>
                        <button
                          onClick={() => removeItem(item.productId, item.variantId)}
                          className="text-text-wood-muted hover:text-status-cinnabar transition-colors p-0.5"
                          title="Xóa"
                        >
                          <span className="material-symbols-outlined text-[18px]">delete</span>
                        </button>
                      </div>
                      <p className="font-caption-meta text-text-wood-muted text-xs mt-0.5">
                        {item.variantName} {item.sku && `• SKU: ${item.sku}`}
                      </p>
                    </div>

                    <div className="flex items-center justify-between mt-2">
                      {/* Quantity Stepper */}
                      <div className="flex items-center border border-border-bamboo rounded-lg bg-surface-container-low overflow-hidden">
                        <button
                          onClick={() => updateQuantity(item.productId, item.variantId, item.quantity - 1)}
                          className="w-6 h-6 flex items-center justify-center text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
                        >
                          <span className="material-symbols-outlined text-[14px]">remove</span>
                        </button>
                        <span className="w-7 text-center font-mono text-xs font-semibold text-text-ink-black">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.productId, item.variantId, item.quantity + 1)}
                          className="w-6 h-6 flex items-center justify-center text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
                        >
                          <span className="material-symbols-outlined text-[14px]">add</span>
                        </button>
                      </div>

                      {/* Price */}
                      <div className="text-right">
                        <span className="font-mono text-sm font-bold text-primary">
                          {formatCurrency(item.price * item.quantity)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Drawer Footer with Checkout Button */}
          {items.length > 0 && (
            <div className="p-6 border-t border-border-bamboo bg-bg-warm-sand/30 space-y-3">
              <div className="flex items-center justify-between text-sm">
                <span className="text-text-wood-muted">Tạm tính:</span>
                <span className="font-mono font-bold text-text-ink-black text-base">{formatCurrency(subtotal)}</span>
              </div>
              <div className="flex items-center justify-between text-xs text-text-wood-muted">
                <span>Phí vận chuyển:</span>
                <span>{isFreeShipping ? <strong className="text-accent-lotus-jade">Miễn Phí</strong> : '30.000 ₫ (Tính tại bước thanh toán)'}</span>
              </div>
              <div className="pt-2 border-t border-border-bamboo/40 flex items-center justify-between">
                <span className="font-serif font-bold text-primary text-base">Tổng tiền:</span>
                <span className="font-mono text-xl font-bold text-primary">
                  {formatCurrency(subtotal + (isFreeShipping ? 0 : 30000))}
                </span>
              </div>

              <div className="pt-2 flex flex-col gap-2">
                <Link
                  href="/checkout"
                  onClick={closeCart}
                  className="w-full py-3.5 px-4 rounded-xl bg-primary text-secondary-fixed font-label-lg font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg"
                >
                  <span>Tiến Hành Đặt Hàng</span>
                  <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                </Link>
                <button
                  onClick={closeCart}
                  className="w-full py-2 text-center text-xs text-text-wood-muted hover:text-primary transition-colors"
                >
                  Tiếp tục chọn thêm sản phẩm
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

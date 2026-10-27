'use client';

import React, { useState, useEffect } from 'react';
import { catalogService } from '@/services/catalogService';
import { ProductResponse, ProductVariantResponse } from '@/types';
import toast from 'react-hot-toast';

export default function AdminInventoryPage() {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Update Inventory Modal
  const [updatingVariant, setUpdatingVariant] = useState<{
    variant: ProductVariantResponse;
    productName: string;
  } | null>(null);
  const [quantity, setQuantity] = useState<number>(0);
  const [threshold, setThreshold] = useState<number>(10);
  const [isUpdating, setIsUpdating] = useState(false);

  const loadInventory = async () => {
    setIsLoading(true);
    try {
      const prodsPage = await catalogService.getProducts({ size: 100 });
      setProducts(prodsPage.content || []);
    } catch (e) {
      console.error('Failed to load inventory:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadInventory();
  }, []);

  const openUpdateModal = (prod: ProductResponse, v: ProductVariantResponse) => {
    setUpdatingVariant({ variant: v, productName: prod.name });
    setQuantity(v.availableStock ?? v.stockQuantity ?? 20);
    setThreshold(10);
  };

  const handleUpdateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!updatingVariant) return;
    setIsUpdating(true);
    try {
      await catalogService.updateInventory(updatingVariant.variant.id, {
        quantity,
        lowStockThreshold: threshold,
      });
      toast.success(`Đã cập nhật tồn kho biến thể ${updatingVariant.variant.sku}!`);
      setUpdatingVariant(null);
      loadInventory();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể cập nhật tồn kho');
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-2">
        <h1 className="font-serif text-2xl lg:text-3xl font-bold text-primary">
          Quản Lý Tồn Kho & Khóa Phân Tán Redisson
        </h1>
        <p className="text-xs text-text-wood-muted">
          Kiểm soát số lượng tồn khả dụng (availableStock), số lượng giữ chỗ (reservedQuantity) và ngưỡng cảnh báo
        </p>
      </div>

      {/* Table */}
      <div className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-bg-warm-sand/80 text-text-wood-muted uppercase font-mono border-b border-border-bamboo">
              <tr>
                <th className="p-4">Sản Phẩm</th>
                <th className="p-4">Biến Thể</th>
                <th className="p-4">Mã SKU</th>
                <th className="p-4">Tồn Khả Dụng</th>
                <th className="p-4">Trạng Thái Kho</th>
                <th className="p-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-bamboo/50">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-text-wood-muted">
                    <span className="material-symbols-outlined text-[32px] text-primary animate-spin">spa</span>
                    <p className="mt-2 font-serif text-primary">Đang tải danh sách kho...</p>
                  </td>
                </tr>
              ) : products.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-12 text-center text-text-wood-muted">
                    Chưa có dữ liệu tồn kho.
                  </td>
                </tr>
              ) : (
                products.flatMap((prod) =>
                  (prod.variants || []).map((v) => {
                    const stock = v.availableStock ?? v.stockQuantity ?? 15;
                    const isLow = stock <= 10;
                    return (
                      <tr key={v.id} className="hover:bg-surface-container-low transition-colors">
                        <td className="p-4 font-bold text-text-ink-black max-w-xs truncate">
                          {prod.name}
                        </td>
                        <td className="p-4 text-text-wood-muted font-medium">{v.variantName}</td>
                        <td className="p-4 font-mono font-bold text-primary">{v.sku}</td>
                        <td className="p-4 font-mono font-bold text-base">
                          <span className={isLow ? 'text-status-cinnabar' : 'text-primary'}>
                            {stock}
                          </span>
                        </td>
                        <td className="p-4">
                          {isLow ? (
                            <span className="px-2.5 py-1 rounded-full bg-status-cinnabar/15 text-status-cinnabar font-semibold inline-flex items-center gap-1">
                              <span className="w-1.5 h-1.5 rounded-full bg-status-cinnabar animate-ping" />
                              Sắp Hết
                            </span>
                          ) : (
                            <span className="px-2.5 py-1 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade font-semibold inline-flex items-center gap-1">
                              <span className="w-1.5 h-1.5 rounded-full bg-accent-lotus-jade" />
                              Còn Hàng
                            </span>
                          )}
                        </td>
                        <td className="p-4 text-right">
                          <button
                            onClick={() => openUpdateModal(prod, v)}
                            className="px-3 py-1.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm"
                          >
                            Điều Chỉnh Kho
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Update Inventory */}
      {updatingVariant && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 sm:p-8 shadow-2xl space-y-4">
            <h3 className="font-serif text-lg font-bold text-primary">
              Cập Nhật Tồn Kho — {updatingVariant.variant.sku}
            </h3>
            <p className="text-xs text-text-wood-muted line-clamp-1">{updatingVariant.productName}</p>

            <form onSubmit={handleUpdateSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                  Số Lượng Tồn Kho Mới *
                </label>
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                  required
                  min={0}
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs font-mono text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                  Ngưỡng Cảnh Báo Sắp Hết (Low Stock Threshold) *
                </label>
                <input
                  type="number"
                  value={threshold}
                  onChange={(e) => setThreshold(Number(e.target.value))}
                  required
                  min={1}
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs font-mono text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-bamboo">
                <button
                  type="button"
                  onClick={() => setUpdatingVariant(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isUpdating}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-bold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isUpdating ? 'Đang lưu...' : 'Lưu Tồn Kho'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

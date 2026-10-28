'use client';

import React, { useState, useEffect } from 'react';
import { orderService } from '@/services/orderService';
import { CouponResponse } from '@/types';
import { formatCurrency, formatDate } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function AdminCouponsPage() {
  const [coupons, setCoupons] = useState<CouponResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCoupon, setEditingCoupon] = useState<CouponResponse | null>(null);

  // Form State
  const [code, setCode] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [discountType, setDiscountType] = useState<'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING'>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState<number>(10);
  const [minOrderValue, setMinOrderValue] = useState<number>(300000);
  const [maxDiscountAmount, setMaxDiscountAmount] = useState<number>(50000);
  const [startDate, setStartDate] = useState(new Date().toISOString().slice(0, 10));
  const [endDate, setEndDate] = useState(
    new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
  );
  const [usageLimit, setUsageLimit] = useState<number>(100);
  const [isSaving, setIsSaving] = useState(false);

  const loadCoupons = async () => {
    setIsLoading(true);
    try {
      const res = await orderService.getAdminCoupons(0, 50);
      setCoupons(res.content || []);
    } catch (e) {
      console.error('Failed to load coupons:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCoupons();
  }, []);

  const openCreateModal = () => {
    setEditingCoupon(null);
    setCode('MOCHUONG10');
    setTitle('Giảm 10% Cho Khách Hàng Mới');
    setDescription('Áp dụng cho mọi phẩm hương nguyên chất');
    setDiscountType('PERCENTAGE');
    setDiscountValue(10);
    setMinOrderValue(300000);
    setMaxDiscountAmount(50000);
    setUsageLimit(200);
    setIsModalOpen(true);
  };

  const openEditModal = (c: CouponResponse) => {
    setEditingCoupon(c);
    setCode(c.code);
    setTitle(c.title || (c as any).name || '');
    setDescription(c.description || '');
    setDiscountType((c.discountType || (c as any).type || 'PERCENTAGE') as any);
    setDiscountValue(c.discountValue ?? (c as any).value ?? 10);
    setMinOrderValue(c.minOrderValue ?? 0);
    setMaxDiscountAmount(c.maxDiscountAmount || 0);
    const startRaw = c.startDate || (c as any).startsAt || '';
    const endRaw = c.endDate || (c as any).expiresAt || '';
    setStartDate(startRaw ? startRaw.slice(0, 10) : '');
    setEndDate(endRaw ? endRaw.slice(0, 10) : '');
    setUsageLimit(c.usageLimit ?? (c as any).maxUses ?? 100);
    setIsModalOpen(true);
  };

  const handleSaveCoupon = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim() || !title.trim()) {
      toast.error('Vui lòng nhập mã và tiêu đề khuyến mãi');
      return;
    }

    setIsSaving(true);
    const payload = {
      code: code.trim().toUpperCase(),
      name: title.trim(),
      title: title.trim(),
      description: description.trim(),
      type: discountType,
      discountType,
      value: discountValue,
      discountValue,
      minOrderValue,
      maxDiscountAmount: maxDiscountAmount || undefined,
      startDate: `${startDate}T00:00:00`,
      startsAt: `${startDate}T00:00:00`,
      endDate: `${endDate}T23:59:59`,
      expiresAt: `${endDate}T23:59:59`,
      usageLimit,
      maxUses: usageLimit,
      isActive: true,
    };

    try {
      if (editingCoupon) {
        await orderService.updateCoupon(editingCoupon.id, payload);
        toast.success('Đã cập nhật mã giảm giá thành công!');
      } else {
        await orderService.createCoupon(payload);
        toast.success('Đã tạo mã giảm giá mới!');
      }
      setIsModalOpen(false);
      loadCoupons();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu mã giảm giá');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Xác nhận xóa mã giảm giá này?')) return;
    try {
      await orderService.deleteCoupon(id);
      toast.success('Đã xóa mã giảm giá');
      loadCoupons();
    } catch (e) {
      toast.error('Không thể xóa mã');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-serif text-2xl lg:text-3xl font-bold text-primary">
            Quản Lý Mã Giảm Giá & Voucher
          </h1>
          <p className="text-xs text-text-wood-muted mt-0.5">
            Cấu hình mã khuyến mãi phần trăm (%), số tiền cố định và miễn phí vận chuyển
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="px-5 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm flex items-center gap-2 self-start sm:self-auto"
        >
          <span className="material-symbols-outlined text-[18px]">add_circle</span>
          <span>Tạo Mã Giảm Giá Mới</span>
        </button>
      </div>

      {/* Coupons Table */}
      <div className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-bg-warm-sand/80 text-text-wood-muted uppercase font-mono border-b border-border-bamboo">
              <tr>
                <th className="p-4">Mã Code</th>
                <th className="p-4">Tiêu Đề & Mô Tả</th>
                <th className="p-4">Loại & Giá Trị</th>
                <th className="p-4">Đơn Tối Thiểu</th>
                <th className="p-4">Lượt Dùng</th>
                <th className="p-4">Hạn Sử Dụng</th>
                <th className="p-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-bamboo/50">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-text-wood-muted">
                    <span className="material-symbols-outlined text-[32px] text-primary animate-spin">spa</span>
                    <p className="mt-2 font-serif text-primary">Đang tải danh sách coupon...</p>
                  </td>
                </tr>
              ) : coupons.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-text-wood-muted">
                    Chưa có mã giảm giá nào. Hãy tạo mã đầu tiên!
                  </td>
                </tr>
              ) : (
                coupons.map((c) => {
                  const cTitle = c.title || (c as any).name || 'Mã giảm giá';
                  const cType = c.discountType || (c as any).type || 'PERCENTAGE';
                  const cValue = c.discountValue ?? (c as any).value ?? 0;
                  const cLimit = c.usageLimit ?? (c as any).maxUses;
                  const cEnd = c.endDate || (c as any).expiresAt;

                  return (
                    <tr key={c.id} className="hover:bg-surface-container-low transition-colors">
                      <td className="p-4">
                        <span className="font-mono font-bold text-primary bg-bg-warm-sand px-2.5 py-1 rounded-lg border border-border-bamboo text-sm">
                          {c.code}
                        </span>
                      </td>

                      <td className="p-4 max-w-xs">
                        <span className="font-bold text-text-ink-black block">{cTitle}</span>
                        <span className="text-[11px] text-text-wood-muted">{c.description}</span>
                      </td>

                      <td className="p-4">
                        <span className="font-semibold text-secondary block">
                          {cType === 'PERCENTAGE'
                            ? `Giảm ${cValue}%`
                            : cType === 'FIXED_AMOUNT'
                            ? `Giảm ${formatCurrency(cValue)}`
                            : 'Miễn Phí Vận Chuyển'}
                        </span>
                        {c.maxDiscountAmount ? (
                          <span className="text-[10px] text-text-wood-muted font-mono">
                            Tối đa: {formatCurrency(c.maxDiscountAmount)}
                          </span>
                        ) : null}
                      </td>

                      <td className="p-4 font-mono font-bold text-text-ink-black">
                        {formatCurrency(c.minOrderValue)}
                      </td>

                      <td className="p-4 font-mono">
                        <span className="font-bold text-primary">{c.usedCount}</span>
                        <span className="text-text-wood-muted"> / {cLimit || '∞'}</span>
                      </td>

                      <td className="p-4 text-[11px] text-text-wood-muted">
                        {formatDate(cEnd)}
                      </td>

                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openEditModal(c)}
                          className="p-1.5 rounded-lg text-text-wood-muted hover:text-primary hover:bg-surface-container"
                          title="Sửa"
                        >
                          <span className="material-symbols-outlined text-[18px]">edit</span>
                        </button>
                        <button
                          onClick={() => handleDelete(c.id)}
                          className="p-1.5 rounded-lg text-text-wood-muted hover:text-status-cinnabar hover:bg-status-cinnabar/10"
                          title="Xóa"
                        >
                          <span className="material-symbols-outlined text-[18px]">delete</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Create / Edit Coupon */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-lg rounded-3xl border border-border-bamboo p-6 sm:p-8 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
              <h3 className="font-serif text-xl font-bold text-primary">
                {editingCoupon ? 'Chỉnh Sửa Mã Giảm Giá' : 'Tạo Mã Giảm Giá Mới'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-text-wood-muted p-1">
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            <form onSubmit={handleSaveCoupon} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Mã Code *</label>
                  <input
                    type="text"
                    value={code}
                    onChange={(e) => setCode(e.target.value.toUpperCase())}
                    placeholder="VD: MOCHUONG10"
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo font-mono font-bold uppercase"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Loại Giảm Giá</label>
                  <select
                    value={discountType}
                    onChange={(e) => setDiscountType(e.target.value as any)}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo font-medium"
                  >
                    <option value="PERCENTAGE">Phần trăm (%)</option>
                    <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
                    <option value="FREE_SHIPPING">Miễn phí giao hàng</option>
                  </select>
                </div>

                <div className="col-span-2">
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Tiêu Đề Voucher *</label>
                  <input
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="VD: Giảm 10% Cho Khách Hàng Mới"
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Giá Trị Giảm *</label>
                  <input
                    type="number"
                    value={discountValue}
                    onChange={(e) => setDiscountValue(Number(e.target.value))}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo font-mono"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Đơn Tối Thiểu (VNĐ)</label>
                  <input
                    type="number"
                    value={minOrderValue}
                    onChange={(e) => setMinOrderValue(Number(e.target.value))}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo font-mono"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Giảm Tối Đa (VNĐ)</label>
                  <input
                    type="number"
                    value={maxDiscountAmount}
                    onChange={(e) => setMaxDiscountAmount(Number(e.target.value))}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo font-mono"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Giới Hạn Lượt Dùng</label>
                  <input
                    type="number"
                    value={usageLimit}
                    onChange={(e) => setUsageLimit(Number(e.target.value))}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo font-mono"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Ngày Bắt Đầu</label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo"
                  />
                </div>

                <div>
                  <label className="block font-bold text-text-wood-muted uppercase mb-1">Ngày Hết Hạn</label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-bamboo">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed font-bold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isSaving ? 'Đang lưu...' : 'Lưu Mã Giảm Giá'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

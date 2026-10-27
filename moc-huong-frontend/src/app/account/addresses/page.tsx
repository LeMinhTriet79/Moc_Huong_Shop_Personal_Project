'use client';

import React, { useState, useEffect } from 'react';
import { identityService } from '@/services/identityService';
import { AddressRequest, AddressResponse } from '@/types';
import toast from 'react-hot-toast';

export default function AddressesPage() {
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  // Form State
  const [recipientName, setRecipientName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [province, setProvince] = useState('TP. Hồ Chí Minh');
  const [district, setDistrict] = useState('Quận 1');
  const [ward, setWard] = useState('Phường Bến Thành');
  const [streetAddress, setStreetAddress] = useState('');
  const [isDefault, setIsDefault] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const fetchAddresses = async () => {
    setIsLoading(true);
    try {
      const data = await identityService.getAddresses();
      setAddresses(data);
    } catch (e) {
      console.error('Failed to load addresses:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAddresses();
  }, []);

  const openAddModal = () => {
    setEditingId(null);
    setRecipientName('');
    setPhoneNumber('');
    setProvince('TP. Hồ Chí Minh');
    setDistrict('Quận 1');
    setWard('Phường Bến Thành');
    setStreetAddress('');
    setIsDefault(addresses.length === 0);
    setIsModalOpen(true);
  };

  const openEditModal = (addr: AddressResponse) => {
    setEditingId(addr.id);
    setRecipientName(addr.recipientName);
    setPhoneNumber(addr.phoneNumber);
    setProvince(addr.province);
    setDistrict(addr.district);
    setWard(addr.ward);
    setStreetAddress(addr.streetAddress);
    setIsDefault(addr.isDefault);
    setIsModalOpen(true);
  };

  const handleSaveAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!recipientName.trim() || !phoneNumber.trim() || !streetAddress.trim()) {
      toast.error('Vui lòng điền đầy đủ các thông tin bắt buộc');
      return;
    }

    setIsSaving(true);
    const payload: AddressRequest = {
      recipientName: recipientName.trim(),
      phoneNumber: phoneNumber.trim(),
      province: province.trim(),
      district: district.trim(),
      ward: ward.trim(),
      streetAddress: streetAddress.trim(),
      isDefault,
    };

    try {
      if (editingId) {
        await identityService.updateAddress(editingId, payload);
        toast.success('Đã cập nhật địa chỉ thành công!');
      } else {
        await identityService.createAddress(payload);
        toast.success('Đã thêm địa chỉ mới thành công!');
      }
      setIsModalOpen(false);
      fetchAddresses();
    } catch (err: any) {
      console.error('Save address error:', err);
      const msg = err.response?.data?.message || 'Có lỗi xảy ra khi lưu địa chỉ';
      toast.error(msg);
    } finally {
      setIsSaving(false);
    }
  };

  const handleSetDefault = async (id: number) => {
    try {
      await identityService.setDefaultAddress(id);
      toast.success('Đã đặt làm địa chỉ mặc định');
      fetchAddresses();
    } catch (e) {
      toast.error('Không thể đặt mặc định');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) return;
    try {
      await identityService.deleteAddress(id);
      toast.success('Đã xóa địa chỉ');
      fetchAddresses();
    } catch (e) {
      toast.error('Không thể xóa địa chỉ này');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="font-serif text-2xl font-bold text-primary">Sổ Địa Chỉ Giao Hàng</h2>
          <p className="text-xs text-text-wood-muted mt-0.5">
            Quản lý danh sách nơi nhận hàng để thuận tiện đặt hàng 1-chạm
          </p>
        </div>

        <button
          onClick={openAddModal}
          className="px-5 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm flex items-center gap-2 self-start sm:self-auto"
        >
          <span className="material-symbols-outlined text-[18px]">add_location_alt</span>
          <span>Thêm Địa Chỉ Mới</span>
        </button>
      </div>

      {/* Address Cards List */}
      {isLoading ? (
        <div className="space-y-4">
          {[...Array(2)].map((_, i) => (
            <div key={i} className="h-28 rounded-2xl bg-bg-warm-sand animate-pulse border border-border-bamboo" />
          ))}
        </div>
      ) : addresses.length === 0 ? (
        <div className="bg-bg-warm-sand/50 rounded-3xl p-12 text-center border border-dashed border-border-bamboo flex flex-col items-center">
          <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-text-wood-muted mb-4 border border-border-bamboo">
            <span className="material-symbols-outlined text-[32px]">home_pin</span>
          </div>
          <h3 className="font-serif text-xl font-bold text-primary mb-2">Chưa có địa chỉ giao hàng</h3>
          <p className="text-xs text-text-wood-muted max-w-sm mb-6">
            Thêm địa chỉ giao hàng ngay để tiết kiệm thời gian cho các đơn hàng tiếp theo.
          </p>
          <button
            onClick={openAddModal}
            className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm"
          >
            Thêm Địa Chỉ Đầu Tiên
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {addresses.map((addr) => (
            <div
              key={addr.id}
              className={`p-6 rounded-3xl border transition-all flex flex-col justify-between gap-4 ${
                addr.isDefault
                  ? 'bg-bg-warm-sand border-primary shadow-sm'
                  : 'bg-surface border-border-bamboo hover:border-primary/50'
              }`}
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <div className="flex items-center gap-2">
                    <h3 className="font-serif font-bold text-base text-primary">{addr.recipientName}</h3>
                    {addr.isDefault && (
                      <span className="px-2 py-0.5 rounded-full bg-accent-lotus-jade text-surface text-[10px] font-bold">
                        Mặc Định
                      </span>
                    )}
                  </div>
                  <span className="font-mono text-xs text-text-wood-muted">{addr.phoneNumber}</span>
                </div>
                <p className="text-xs text-text-ink-black leading-relaxed">
                  {addr.streetAddress}, {addr.ward}, {addr.district}, {addr.province}
                </p>
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-border-bamboo/60 text-xs">
                <div>
                  {!addr.isDefault && (
                    <button
                      onClick={() => handleSetDefault(addr.id)}
                      className="text-secondary hover:text-primary font-semibold transition-colors"
                    >
                      Đặt làm mặc định
                    </button>
                  )}
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => openEditModal(addr)}
                    className="p-1.5 rounded-lg text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
                    title="Chỉnh sửa"
                  >
                    <span className="material-symbols-outlined text-[18px]">edit</span>
                  </button>
                  <button
                    onClick={() => handleDelete(addr.id)}
                    className="p-1.5 rounded-lg text-text-wood-muted hover:text-status-cinnabar hover:bg-status-cinnabar/10 transition-colors"
                    title="Xóa"
                  >
                    <span className="material-symbols-outlined text-[18px]">delete</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-lg rounded-3xl border border-border-bamboo p-6 sm:p-8 shadow-2xl animate-in zoom-in-95 duration-150 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
              <h3 className="font-serif text-xl font-bold text-primary">
                {editingId ? 'Chỉnh Sửa Địa Chỉ' : 'Thêm Địa Chỉ Giao Hàng Mới'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-text-wood-muted hover:text-primary p-1"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            <form onSubmit={handleSaveAddress} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Họ Tên Người Nhận *
                  </label>
                  <input
                    type="text"
                    value={recipientName}
                    onChange={(e) => setRecipientName(e.target.value)}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Số Điện Thoại *
                  </label>
                  <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Địa Chỉ Chi Tiết (Số nhà, đường) *
                  </label>
                  <input
                    type="text"
                    value={streetAddress}
                    onChange={(e) => setStreetAddress(e.target.value)}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Phường / Xã *</label>
                  <input
                    type="text"
                    value={ward}
                    onChange={(e) => setWard(e.target.value)}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Quận / Huyện *</label>
                  <input
                    type="text"
                    value={district}
                    onChange={(e) => setDistrict(e.target.value)}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                    Tỉnh / Thành Phố *
                  </label>
                  <input
                    type="text"
                    value={province}
                    onChange={(e) => setProvince(e.target.value)}
                    required
                    className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  type="checkbox"
                  id="isDefaultCheckbox"
                  checked={isDefault}
                  onChange={(e) => setIsDefault(e.target.checked)}
                  className="rounded text-primary focus:ring-primary h-4 w-4"
                />
                <label htmlFor="isDefaultCheckbox" className="text-xs text-text-ink-black cursor-pointer font-medium">
                  Đặt làm địa chỉ nhận hàng mặc định
                </label>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-border-bamboo">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs text-text-wood-muted hover:bg-surface-container"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors disabled:opacity-50"
                >
                  {isSaving ? 'Đang lưu...' : 'Lưu Địa Chỉ'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

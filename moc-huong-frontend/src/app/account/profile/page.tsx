'use client';

import React, { useState } from 'react';
import { useAuthStore } from '@/store/useAuthStore';
import { identityService } from '@/services/identityService';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const { user, updateUser } = useAuthStore();

  // Profile fields
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');
  const [dateOfBirth, setDateOfBirth] = useState(user?.dateOfBirth || '');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  // Password fields
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsUpdatingProfile(true);
    try {
      const updated = await identityService.updateProfile({
        fullName: fullName.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
        dateOfBirth: dateOfBirth || undefined,
        avatarUrl: avatarUrl.trim() || undefined,
      });
      updateUser(updated);
      toast.success('Đã cập nhật thông tin hồ sơ thành công!', { icon: '🌿' });
    } catch (err: any) {
      console.error('Update profile error:', err);
      const msg = err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật hồ sơ';
      toast.error(msg);
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!oldPassword || !newPassword) {
      toast.error('Vui lòng điền đầy đủ mật khẩu cũ và mới');
      return;
    }
    if (newPassword.length < 6) {
      toast.error('Mật khẩu mới phải có tối thiểu 6 ký tự');
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp');
      return;
    }

    setIsChangingPassword(true);
    try {
      await identityService.changePassword(oldPassword, newPassword);
      toast.success('Đổi mật khẩu thành công! Toàn bộ phiên cũ đã được bảo mật.', { icon: '🔑' });
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      console.error('Change password error:', err);
      const msg = err.response?.data?.message || 'Mật khẩu cũ không chính xác';
      toast.error(msg);
    } finally {
      setIsChangingPassword(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* 1. Profile Information Card */}
      <section className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-6">
        <div className="flex items-center gap-3 pb-3 border-b border-border-bamboo">
          <span className="material-symbols-outlined text-primary text-[24px]">badge</span>
          <div>
            <h2 className="font-serif text-xl font-bold text-primary">Thông Tin Cá Nhân</h2>
            <p className="text-xs text-text-wood-muted">Cập nhật thông tin nhận dạng và địa chỉ liên lạc của bạn</p>
          </div>
        </div>

        <form onSubmit={handleProfileSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Họ Và Tên</label>
              <input
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Email (Cố định)</label>
              <input
                type="email"
                value={user?.email || ''}
                disabled
                className="w-full p-3 bg-surface-container rounded-xl border border-border-bamboo text-sm text-text-wood-muted font-mono cursor-not-allowed opacity-75"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Số Điện Thoại</label>
              <input
                type="tel"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="0909123456"
                className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Ngày Sinh</label>
              <input
                type="date"
                value={dateOfBirth}
                onChange={(e) => setDateOfBirth(e.target.value)}
                className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Đường Dẫn Ảnh Đại Diện (Avatar URL)</label>
              <input
                type="url"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                placeholder="https://..."
                className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={isUpdatingProfile}
              className="px-6 py-3 rounded-xl bg-primary text-secondary-fixed font-label-md text-xs font-bold hover:bg-primary-container transition-all flex items-center gap-2 shadow-sm disabled:opacity-50"
            >
              <span className="material-symbols-outlined text-[18px]">save</span>
              <span>{isUpdatingProfile ? 'Đang lưu...' : 'Lưu Thay Đổi'}</span>
            </button>
          </div>
        </form>
      </section>

      {/* 2. Change Password Card */}
      <section className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-6">
        <div className="flex items-center gap-3 pb-3 border-b border-border-bamboo">
          <span className="material-symbols-outlined text-primary text-[24px]">shield_locked</span>
          <div>
            <h2 className="font-serif text-xl font-bold text-primary">Đổi Mật Khẩu & Bảo Mật</h2>
            <p className="text-xs text-text-wood-muted">Bảo vệ tài khoản bằng mật khẩu có độ phức tạp cao</p>
          </div>
        </div>

        <form onSubmit={handlePasswordSubmit} className="space-y-4 max-w-md">
          <div>
            <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Mật Khẩu Hiện Tại</label>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              placeholder="••••••••"
              required
              className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Mật Khẩu Mới (Tối thiểu 6 ký tự)</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="••••••••"
              required
              className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Xác Nhận Mật Khẩu Mới</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="••••••••"
              required
              className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={isChangingPassword}
              className="px-6 py-3 rounded-xl bg-primary text-secondary-fixed font-label-md text-xs font-bold hover:bg-primary-container transition-all flex items-center gap-2 shadow-sm disabled:opacity-50"
            >
              <span className="material-symbols-outlined text-[18px]">key</span>
              <span>{isChangingPassword ? 'Đang cập nhật...' : 'Cập Nhật Mật Khẩu'}</span>
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

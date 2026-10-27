'use client';

import React, { useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { identityService } from '@/services/identityService';
import toast from 'react-hot-toast';

function ResetPasswordContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const token = searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      toast.error('Mã token khôi phục không hợp lệ');
      return;
    }
    if (newPassword.length < 6) {
      toast.error('Mật khẩu mới cần tối thiểu 6 ký tự');
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp');
      return;
    }

    setIsLoading(true);
    try {
      await identityService.resetPassword(token, newPassword);
      toast.success('Đặt lại mật khẩu thành công! Vui lòng đăng nhập.', { icon: '🔑' });
      router.push('/login');
    } catch (err: any) {
      console.error('Reset password error:', err);
      const msg = err.response?.data?.message || 'Token đã hết hạn hoặc không hợp lệ';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 py-12">
      <div className="max-w-md w-full bg-surface rounded-3xl border border-border-bamboo p-8 sm:p-10 shadow-sm space-y-6">
        <div className="text-center">
          <div className="w-16 h-16 rounded-full bg-bg-warm-sand flex items-center justify-center text-primary mx-auto mb-3 border border-border-bamboo">
            <span className="material-symbols-outlined text-[32px]">password</span>
          </div>
          <h1 className="font-serif text-2xl font-bold text-primary">Đặt Lại Mật Khẩu Mới</h1>
          <p className="font-body-base text-text-wood-muted text-xs mt-1">
            Thiết lập mật khẩu an toàn mới cho tài khoản Mộc Hương của bạn.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
              Mật Khẩu Mới *
            </label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Tối thiểu 6 ký tự"
              required
              className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
              Xác Nhận Mật Khẩu Mới *
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Nhập lại mật khẩu mới"
              required
              className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-3.5 px-4 rounded-xl bg-primary text-secondary-fixed font-label-lg font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
          >
            {isLoading ? 'Đang cập nhật...' : 'Lưu Mật Khẩu Mới'}
          </button>

          <div className="text-center pt-2">
            <Link href="/login" className="text-xs text-text-wood-muted hover:text-primary transition-colors">
              ← Quay lại đăng nhập
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-background">
          <span className="material-symbols-outlined text-[36px] text-primary animate-spin">spa</span>
        </div>
      }
    >
      <ResetPasswordContent />
    </Suspense>
  );
}

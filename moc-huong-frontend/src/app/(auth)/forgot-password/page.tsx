'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { identityService } from '@/services/identityService';
import toast from 'react-hot-toast';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSent, setIsSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) {
      toast.error('Vui lòng nhập email');
      return;
    }

    setIsLoading(true);
    try {
      await identityService.forgotPassword(email.trim());
      setIsSent(true);
      toast.success('Liên kết khôi phục mật khẩu đã được gửi đến email của bạn!', { icon: '📧' });
    } catch (err: any) {
      console.error('Forgot password error:', err);
      const msg = err.response?.data?.message || 'Có lỗi xảy ra khi yêu cầu đặt lại mật khẩu';
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
            <span className="material-symbols-outlined text-[32px]">lock_reset</span>
          </div>
          <h1 className="font-serif text-2xl font-bold text-primary">Quên Mật Khẩu?</h1>
          <p className="font-body-base text-text-wood-muted text-xs mt-1">
            Nhập email tài khoản Mộc Hương của bạn để nhận liên kết thiết lập mật khẩu mới.
          </p>
        </div>

        {isSent ? (
          <div className="bg-bg-warm-sand p-5 rounded-2xl border border-border-bamboo text-center space-y-3">
            <span className="material-symbols-outlined text-accent-lotus-jade text-[36px]">mark_email_read</span>
            <p className="text-xs text-text-ink-black font-semibold">
              Mộc Hương đã gửi thư đặt lại mật khẩu tới <span className="text-primary font-mono">{email}</span>.
            </p>
            <p className="text-[11px] text-text-wood-muted">
              Vui lòng kiểm tra hộp thư đến hoặc mục thư rác (Spam).
            </p>
            <Link
              href="/login"
              className="inline-block mt-2 px-5 py-2 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors"
            >
              Quay Lại Đăng Nhập
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
                Email Của Bạn
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
                className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3.5 px-4 rounded-xl bg-primary text-secondary-fixed font-label-lg font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
            >
              {isLoading ? 'Đang gửi...' : 'Gửi Hướng Dẫn Khôi Phục'}
            </button>

            <div className="text-center pt-2">
              <Link href="/login" className="text-xs text-text-wood-muted hover:text-primary transition-colors">
                ← Quay lại trang đăng nhập
              </Link>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

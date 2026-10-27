'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { identityService } from '@/services/identityService';
import toast from 'react-hot-toast';

export default function RegisterPage() {
  const router = useRouter();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim() || !email.trim() || !password.trim()) {
      toast.error('Vui lòng điền đầy đủ các thông tin bắt buộc');
      return;
    }

    if (password !== confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp');
      return;
    }

    if (password.length < 6) {
      toast.error('Mật khẩu cần tối thiểu 6 ký tự');
      return;
    }

    setIsLoading(true);
    try {
      await identityService.register(email.trim(), password, fullName.trim());
      toast.success('Đăng ký tài khoản thành công! Vui lòng kiểm tra email kích hoạt.', {
        icon: '🌿',
        duration: 5000,
      });
      router.push('/login');
    } catch (err: any) {
      console.error('Register error:', err);
      const msg = err.response?.data?.message || 'Không thể tạo tài khoản. Vui lòng thử lại';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-12 bg-background">
      {/* Left Column: Visual */}
      <div className="hidden lg:flex lg:col-span-5 bg-bg-agarwood-dark text-surface-bright relative flex-col justify-between p-12 overflow-hidden border-r border-border-bamboo/20">
        <div className="absolute inset-0 bg-cover bg-center opacity-30 mix-blend-overlay" style={{ backgroundImage: `url('https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=1000')` }} />
        <div className="relative z-10">
          <Link href="/" className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-full bg-secondary-fixed/20 flex items-center justify-center text-secondary-fixed">
              <span className="material-symbols-outlined text-[22px]">spa</span>
            </div>
            <span className="font-serif text-xl font-bold tracking-wider text-surface-bright">MỘC HƯƠNG</span>
          </Link>
        </div>

        <div className="relative z-10 space-y-4 max-w-sm">
          <span className="font-caption-meta text-secondary-fixed uppercase tracking-widest text-xs font-semibold">
            Gia Nhập Cộng Đồng Thiền Hương
          </span>
          <h2 className="font-serif text-3xl font-bold leading-tight text-surface-bright">
            Khởi Đầu Hành Trình An Lạc
          </h2>
          <p className="font-body-sm text-surface-variant text-sm leading-relaxed">
            Đăng ký để trở thành bạn hữu thân thiết của Mộc Hương, tích lũy điểm thưởng và nhận thông tin về các mẻ trầm rừng mới nhất.
          </p>
        </div>

        <div className="relative z-10 text-xs text-surface-dim/60 font-caption-meta">
          © 2026 Mộc Hương Aromatics.
        </div>
      </div>

      {/* Right Column: Form */}
      <div className="lg:col-span-7 flex items-center justify-center p-6 sm:p-12">
        <div className="w-full max-w-md bg-surface p-8 sm:p-10 rounded-3xl border border-border-bamboo shadow-sm space-y-6">
          <div>
            <h1 className="font-serif text-3xl font-bold text-primary">Tạo Tài Khoản Mới</h1>
            <p className="font-body-base text-text-wood-muted text-sm mt-1">
              Đã có tài khoản?{' '}
              <Link href="/login" className="text-secondary font-semibold hover:underline">
                Đăng nhập ngay
              </Link>
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
                Họ Và Tên *
              </label>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
                  person
                </span>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="VD: Lê Minh Triết"
                  required
                  className="w-full pl-10 pr-4 py-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
                Địa Chỉ Email *
              </label>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
                  mail
                </span>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  required
                  className="w-full pl-10 pr-4 py-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
                Mật Khẩu *
              </label>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
                  lock
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Tối thiểu 6 ký tự"
                  required
                  className="w-full pl-10 pr-10 py-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-wood-muted hover:text-primary p-1"
                >
                  <span className="material-symbols-outlined text-[18px]">
                    {showPassword ? 'visibility_off' : 'visibility'}
                  </span>
                </button>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-text-wood-muted mb-1.5">
                Xác Nhận Mật Khẩu *
              </label>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
                  lock_reset
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Nhập lại mật khẩu"
                  required
                  className="w-full pl-10 pr-4 py-3 bg-surface-container-low rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3.5 px-4 rounded-xl bg-primary text-secondary-fixed font-label-lg font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
            >
              {isLoading ? (
                <>
                  <span className="material-symbols-outlined text-[20px] animate-spin">spa</span>
                  <span>Đang đăng ký...</span>
                </>
              ) : (
                <>
                  <span>Hoàn Tất Đăng Ký</span>
                  <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                </>
              )}
            </button>
          </form>

          <p className="text-[11px] text-text-wood-muted text-center leading-relaxed">
            Bằng việc nhấn Đăng Ký, bạn đồng ý với Điều Khoản Dịch Vụ và Chính Sách Bảo Mật của Mộc Hương.
          </p>
        </div>
      </div>
    </div>
  );
}

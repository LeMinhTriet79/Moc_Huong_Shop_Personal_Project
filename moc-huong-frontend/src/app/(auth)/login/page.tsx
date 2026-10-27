'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { identityService } from '@/services/identityService';
import { useAuthStore } from '@/store/useAuthStore';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuthStore();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) {
      toast.error('Vui lòng nhập đầy đủ email và mật khẩu');
      return;
    }

    setIsLoading(true);
    try {
      const authData = await identityService.login(email.trim(), password);
      login(authData);
      toast.success(`Chào mừng ${authData.fullName} quay trở lại!`, { icon: '🌿' });

      // Redirect based on role
      if (authData.role === 'STAFF' || authData.role === 'ADMIN') {
        router.push('/admin');
      } else {
        router.push('/account/profile');
      }
    } catch (err: any) {
      console.error('Login error:', err);
      const msg = err.response?.data?.message || 'Email hoặc mật khẩu không chính xác';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleDemoLogin = async () => {
    toast.error('Đăng nhập Google yêu cầu cấu hình Google Client ID trên môi trường production');
  };

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-12 bg-background">
      {/* Left Column: Atmospheric Editorial Visual (5 Cols) */}
      <div className="hidden lg:flex lg:col-span-5 bg-bg-agarwood-dark text-surface-bright relative flex-col justify-between p-12 overflow-hidden border-r border-border-bamboo/20">
        <div className="absolute inset-0 bg-cover bg-center opacity-30 mix-blend-overlay" style={{ backgroundImage: `url('https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=1000')` }} />
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
            Không Gian Tĩnh Tâm
          </span>
          <h2 className="font-serif text-3xl font-bold leading-tight text-surface-bright">
            “Hương Từ Tâm • Mộc Từ Đất”
          </h2>
          <p className="font-body-sm text-surface-variant text-sm leading-relaxed">
            Đăng nhập để đồng bộ giỏ hàng, tra cứu lịch sử chuyển giao trầm phẩm và nhận những đặc quyền thanh tịnh dành riêng cho quý khách.
          </p>
        </div>

        <div className="relative z-10 text-xs text-surface-dim/60 font-caption-meta">
          © 2026 Mộc Hương Aromatics. Bảo tồn di sản Trầm Việt.
        </div>
      </div>

      {/* Right Column: Form (7 Cols) */}
      <div className="lg:col-span-7 flex items-center justify-center p-6 sm:p-12">
        <div className="w-full max-w-md bg-surface p-8 sm:p-10 rounded-3xl border border-border-bamboo shadow-sm space-y-6">
          <div>
            <h1 className="font-serif text-3xl font-bold text-primary">Đăng Nhập Tài Khoản</h1>
            <p className="font-body-base text-text-wood-muted text-sm mt-1">
              Chưa có tài khoản?{' '}
              <Link href="/register" className="text-secondary font-semibold hover:underline">
                Đăng ký thành viên
              </Link>
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
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
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-bold uppercase tracking-wider text-text-wood-muted">Mật Khẩu *</label>
                <Link href="/forgot-password" className="text-xs text-secondary hover:underline">
                  Quên mật khẩu?
                </Link>
              </div>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
                  lock
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
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

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3.5 px-4 rounded-xl bg-primary text-secondary-fixed font-label-lg font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
            >
              {isLoading ? (
                <>
                  <span className="material-symbols-outlined text-[20px] animate-spin">spa</span>
                  <span>Đang đăng nhập...</span>
                </>
              ) : (
                <>
                  <span>Vào Không Gian Mộc Hương</span>
                  <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                </>
              )}
            </button>
          </form>

          <div className="relative flex items-center justify-center">
            <div className="border-t border-border-bamboo w-full" />
            <span className="bg-surface px-3 text-xs text-text-wood-muted font-medium uppercase absolute">Hoặc</span>
          </div>

          <button
            type="button"
            onClick={handleGoogleDemoLogin}
            className="w-full py-3 px-4 rounded-xl bg-surface border border-border-bamboo text-text-ink-black font-label-md text-xs font-semibold hover:bg-surface-container transition-colors flex items-center justify-center gap-2 shadow-sm"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24">
              <path
                fill="#EA4335"
                d="M12 5c1.6 0 3 .6 4.1 1.6l3.1-3.1C17.3 1.7 14.8 1 12 1 7.5 1 3.7 3.6 1.9 7.4l3.7 2.9C6.5 7.4 9 5 12 5z"
              />
              <path
                fill="#4285F4"
                d="M23.5 12.3c0-.8-.1-1.6-.2-2.3H12v4.5h6.5c-.3 1.5-1.1 2.8-2.4 3.7l3.7 2.9c2.2-2 3.7-5.1 3.7-8.8z"
              />
              <path
                fill="#FBBC05"
                d="M5.6 14.7c-.2-.7-.4-1.5-.4-2.7s.1-2 .4-2.7L1.9 6.4C.7 8.8 0 10.4 0 12s.7 3.2 1.9 5.6l3.7-2.9z"
              />
              <path
                fill="#34A853"
                d="M12 23c3.2 0 6-1.1 8-3l-3.7-2.9c-1.1.7-2.5 1.2-4.3 1.2-3 0-5.5-2.4-6.4-5.3L1.9 16c1.8 3.8 5.6 7 10.1 7z"
              />
            </svg>
            <span>Đăng nhập nhanh với Google</span>
          </button>
        </div>
      </div>
    </div>
  );
}

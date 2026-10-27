'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';
import toast from 'react-hot-toast';

export default function AccountLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isAuthenticated, role, logout } = useAuthStore();

  useEffect(() => {
    // If user is not authenticated, redirect to login
    if (!isAuthenticated) {
      router.push('/login?redirect=' + encodeURIComponent(pathname));
    }
  }, [isAuthenticated, pathname, router]);

  if (!isAuthenticated || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <span className="material-symbols-outlined text-[40px] text-primary animate-spin">spa</span>
          <p className="font-serif text-primary text-base">Đang xác thực quyền truy cập...</p>
        </div>
      </div>
    );
  }

  const handleLogout = async () => {
    await logout();
    toast.success('Đã đăng xuất tài khoản');
    router.push('/');
  };

  const navLinks = [
    { label: 'Hồ Sơ Cá Nhân', href: '/account/profile', icon: 'person' },
    { label: 'Lịch Sử Đơn Hàng', href: '/account/orders', icon: 'receipt_long' },
    { label: 'Sổ Địa Chỉ Giao Hàng', href: '/account/addresses', icon: 'location_on' },
    { label: 'Danh Sách Yêu Thích', href: '/account/wishlist', icon: 'favorite' },
  ];

  return (
    <div className="w-full bg-background min-h-screen py-8 lg:py-12">
      <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
        {/* Sanctuary Header Banner */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 pb-6 mb-8 border-b border-border-bamboo">
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <span className="w-2 h-2 rounded-full bg-primary" />
              <span className="font-caption-meta text-xs uppercase tracking-widest text-secondary font-semibold">
                Tâm Thức An Nhiên • Dấu Ấn Riêng Bản
              </span>
            </div>
            <h1 className="font-serif text-3xl lg:text-4xl text-primary font-bold">
              Không Gian Cá Nhân & Tĩnh Tâm
            </h1>
            <p className="font-body-base text-text-wood-muted text-sm mt-1 max-w-xl">
              Nơi gìn giữ dấu ấn trải nghiệm, chốn về thân quen và tâm ý gửi gắm vào từng vật phẩm Mộc Hương.
            </p>
          </div>

          <div className="bg-bg-warm-sand px-5 py-2.5 rounded-2xl border border-border-bamboo shadow-sm flex items-center gap-3 self-start md:self-auto">
            <div className="w-9 h-9 rounded-full bg-secondary-fixed text-primary flex items-center justify-center font-bold">
              <span className="material-symbols-outlined text-[20px]">spa</span>
            </div>
            <div>
              <span className="block text-[11px] uppercase tracking-wider text-text-wood-muted font-bold">
                Cảnh Giới Tích Lũy
              </span>
              <span className="font-serif text-sm font-bold text-primary">Trầm Hương Cổ Thụ</span>
            </div>
          </div>
        </div>

        {/* 12-Column Responsive Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Left Column: Sidebar (3 Cols) */}
          <aside className="lg:col-span-4 xl:col-span-3 lg:sticky lg:top-28 flex flex-col gap-5">
            {/* User Identity Card */}
            <div className="bg-bg-warm-sand p-6 rounded-3xl border border-border-bamboo shadow-sm text-center relative overflow-hidden">
              <div className="relative p-1 rounded-full bg-secondary-fixed/40 inline-block mb-3">
                <div className="w-20 h-20 rounded-full bg-primary text-secondary-fixed flex items-center justify-center font-bold text-2xl overflow-hidden border-2 border-secondary-fixed">
                  {user.avatarUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={user.avatarUrl} alt={user.fullName} className="w-full h-full object-cover" />
                  ) : (
                    user.fullName?.charAt(0) || 'M'
                  )}
                </div>
              </div>

              <h2 className="font-serif text-lg font-bold text-primary">{user.fullName}</h2>
              <p className="font-mono text-xs text-text-wood-muted truncate mt-0.5">{user.email}</p>

              <div className="flex items-center justify-center gap-2 mt-3">
                <span className="px-2.5 py-0.5 rounded-full bg-primary text-secondary-fixed text-[11px] font-bold uppercase tracking-wider">
                  {role || 'CUSTOMER'}
                </span>
                <span className="px-2.5 py-0.5 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade text-[11px] font-semibold flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-accent-lotus-jade" />
                  Đã xác thực
                </span>
              </div>
            </div>

            {/* Navigation Menu */}
            <nav className="bg-surface rounded-2xl p-2 border border-border-bamboo shadow-sm space-y-1">
              {navLinks.map((link) => {
                const isActive = pathname === link.href;
                return (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`w-full flex items-center justify-between px-4 py-3 rounded-xl font-label-md text-xs font-semibold transition-all ${
                      isActive
                        ? 'bg-primary text-secondary-fixed shadow-md'
                        : 'text-text-ink-black hover:bg-surface-container'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <span className="material-symbols-outlined text-[20px]">{link.icon}</span>
                      <span>{link.label}</span>
                    </div>
                    <span className="material-symbols-outlined text-[16px] opacity-60">chevron_right</span>
                  </Link>
                );
              })}

              {(role === 'STAFF' || role === 'ADMIN') && (
                <Link
                  href="/admin"
                  className="w-full flex items-center justify-between px-4 py-3 rounded-xl font-label-md text-xs font-bold text-secondary bg-secondary-fixed/20 hover:bg-secondary-fixed/30 transition-all border border-secondary/30 mt-2"
                >
                  <div className="flex items-center gap-2.5">
                    <span className="material-symbols-outlined text-[20px]">admin_panel_settings</span>
                    <span>Cổng Quản Trị ({role})</span>
                  </div>
                  <span className="material-symbols-outlined text-[16px]">open_in_new</span>
                </Link>
              )}

              <div className="pt-2 border-t border-border-bamboo">
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-status-cinnabar hover:bg-status-cinnabar/10 transition-colors text-left"
                >
                  <span className="material-symbols-outlined text-[18px]">logout</span>
                  <span>Đăng Xuất</span>
                </button>
              </div>
            </nav>
          </aside>

          {/* Right Column: Tab Content (9 Cols) */}
          <main className="lg:col-span-8 xl:col-span-9">{children}</main>
        </div>
      </div>
    </div>
  );
}

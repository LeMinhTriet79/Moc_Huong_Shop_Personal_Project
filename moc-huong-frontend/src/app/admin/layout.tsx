'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';
import toast from 'react-hot-toast';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isAuthenticated, role, logout } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login?redirect=' + encodeURIComponent(pathname));
    } else if (role !== 'STAFF' && role !== 'ADMIN') {
      toast.error('Khu vực dành riêng cho Nhân viên (STAFF) và Quản trị viên (ADMIN)');
      router.push('/');
    } else if (role === 'STAFF' && (pathname.startsWith('/admin/users') || pathname.startsWith('/admin/coupons'))) {
      toast.error('Chức năng này chỉ dành riêng cho Quản trị viên (ADMIN)');
      router.push('/admin');
    }
  }, [isAuthenticated, role, pathname, router]);

  if (!isAuthenticated || (role !== 'STAFF' && role !== 'ADMIN')) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <span className="material-symbols-outlined text-[40px] text-primary animate-spin">spa</span>
          <p className="font-serif text-primary text-base">Đang kiểm tra quyền quản trị...</p>
        </div>
      </div>
    );
  }

  const handleLogout = async () => {
    await logout();
    toast.success('Đã đăng xuất khỏi cổng quản trị');
    router.push('/');
  };

  const navOperations = [
    { label: 'Tổng Quan KPI', href: '/admin', icon: 'dashboard' },
    { label: 'Quản Lý Đơn Hàng', href: '/admin/orders', icon: 'receipt_long' },
    { label: 'Sản Phẩm & Biến Thể', href: '/admin/products', icon: 'inventory_2' },
    { label: 'Quản Lý Tồn Kho', href: '/admin/inventory', icon: 'warehouse' },
    { label: 'Kiểm Duyệt Đánh Giá', href: '/admin/reviews', icon: 'rate_review' },
    ...(role === 'ADMIN' ? [{ label: 'Mã Giảm Giá & Voucher', href: '/admin/coupons', icon: 'loyalty' }] : []),
  ];

  const navSystem = [
    { label: 'Người Dùng & Phân Quyền', href: '/admin/users', icon: 'manage_accounts' },
  ];

  return (
    <div className="min-h-screen bg-surface flex">
      {/* Fixed Admin Sidebar */}
      <aside className="fixed left-0 top-0 h-full w-72 bg-bg-agarwood-dark text-surface-bright z-50 flex flex-col justify-between shadow-2xl border-r border-border-bamboo/20">
        <div className="flex flex-col flex-1 overflow-y-auto">
          {/* Brand Header */}
          <div className="p-6 flex items-center gap-3 border-b border-border-bamboo/15">
            <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-secondary-fixed shadow-md border border-secondary/40">
              <span className="material-symbols-outlined text-[24px]">spa</span>
            </div>
            <div>
              <span className="block font-serif text-lg font-bold tracking-wider text-surface-bright leading-none">
                MỘC HƯƠNG
              </span>
              <span className="block text-[10px] uppercase font-caption-meta text-secondary-fixed tracking-widest mt-1">
                Cổng Quản Trị Am Trầm
              </span>
            </div>
          </div>

          {/* Section: Operations */}
          <div className="px-6 pt-5 pb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-text-wood-muted">
              Nghiệp Vụ Bán Hàng
            </span>
          </div>

          <nav className="flex flex-col px-3 gap-1">
            {navOperations.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-label-md text-xs font-semibold transition-all ${
                    isActive
                      ? 'bg-primary text-secondary-fixed shadow-md border border-secondary/30'
                      : 'text-surface-variant hover:bg-surface-container-highest/10 hover:text-surface-bright'
                  }`}
                >
                  <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          {/* Section: System Admin (ADMIN only) */}
          {role === 'ADMIN' && (
            <>
              <div className="px-6 pt-6 pb-2 flex items-center justify-between">
                <span className="text-[11px] font-bold uppercase tracking-wider text-text-wood-muted">
                  Quản Trị Hệ Thống
                </span>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-secondary-fixed/20 text-secondary-fixed font-bold">
                  ADMIN
                </span>
              </div>

              <nav className="flex flex-col px-3 gap-1">
                {navSystem.map((item) => {
                  const isActive = pathname === item.href;
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-label-md text-xs font-semibold transition-all ${
                        isActive
                          ? 'bg-primary text-secondary-fixed shadow-md border border-secondary/30'
                          : 'text-surface-variant hover:bg-surface-container-highest/10 hover:text-surface-bright'
                      }`}
                    >
                      <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
                      <span>{item.label}</span>
                    </Link>
                  );
                })}
              </nav>
            </>
          )}
        </div>

        {/* Bottom User Bar */}
        <div className="p-4 bg-primary/60 m-3 rounded-2xl flex items-center justify-between border border-border-bamboo/20">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-9 h-9 rounded-full bg-secondary text-surface-bright flex items-center justify-center font-bold text-xs shrink-0">
              {user?.fullName?.charAt(0) || 'A'}
            </div>
            <div className="flex flex-col overflow-hidden">
              <span className="text-xs font-bold text-surface-bright truncate max-w-[120px]">
                {user?.fullName}
              </span>
              <span className="text-[10px] font-mono text-secondary-fixed font-semibold">
                {role}
              </span>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="p-2 rounded-xl text-surface-variant hover:text-status-cinnabar hover:bg-status-cinnabar/10 transition-colors"
            title="Đăng xuất"
          >
            <span className="material-symbols-outlined text-[20px]">logout</span>
          </button>
        </div>
      </aside>

      {/* Main Container */}
      <div className="pl-72 flex-1 flex flex-col min-h-screen">
        {/* Top Header */}
        <header className="sticky top-0 z-40 h-16 bg-surface/90 backdrop-blur-xl border-b border-border-bamboo flex items-center justify-between px-8 shadow-sm">
          <div className="flex items-center gap-3">
            <span className="w-2 h-2 rounded-full bg-accent-lotus-jade animate-pulse" />
            <span className="text-xs font-mono text-text-wood-muted">
              CỔNG QUẢN TRỊ MỘC HƯƠNG • MICROSERVICES BACKEND ONLINE
            </span>
          </div>

          <div className="flex items-center gap-4">
            <Link
              href="/"
              target="_blank"
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-bg-warm-sand text-primary hover:bg-surface-container font-label-md text-xs font-semibold transition-colors border border-border-bamboo"
            >
              <span className="material-symbols-outlined text-[16px]">storefront</span>
              <span>Xem Cửa Hàng</span>
            </Link>

            <span className="px-2.5 py-1 rounded-full bg-primary text-secondary-fixed text-[11px] font-mono font-bold">
              {role}
            </span>
          </div>
        </header>

        {/* Admin Page Content */}
        <div className="flex-1 p-6 lg:p-8 bg-surface-container-low/40">{children}</div>
      </div>
    </div>
  );
}

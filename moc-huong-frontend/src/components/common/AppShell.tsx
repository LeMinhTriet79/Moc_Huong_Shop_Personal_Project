'use client';

import React from 'react';
import { usePathname } from 'next/navigation';
import Header from '@/components/common/Header';
import Footer from '@/components/common/Footer';
import CartDrawer from '@/components/cart/CartDrawer';
import AiChatWidget from '@/components/common/AiChatWidget';

const FULLSCREEN_AUTH_ROUTES = [
  '/login',
  '/register',
  '/forgot-password',
  '/reset-password',
  '/verify-email',
];

export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname() || '';

  const isAdminRoute = pathname.startsWith('/admin');
  const isFullscreenAuthRoute = FULLSCREEN_AUTH_ROUTES.some((route) =>
    pathname.startsWith(route)
  );

  // 1. Giao diện Cổng Quản Trị (/admin/*): Không hiển thị Header/Footer/CartDrawer của khách hàng
  if (isAdminRoute) {
    return (
      <div className="min-h-screen bg-surface text-text-ink-black">
        {children}
      </div>
    );
  }

  // 2. Giao diện Đăng nhập / Đăng ký Fullscreen: Giữ nguyên thiết kế Split-screen 2 cột tinh tế
  if (isFullscreenAuthRoute) {
    return (
      <div className="min-h-screen bg-background text-text-ink-black">
        {children}
        <AiChatWidget />
      </div>
    );
  }

  // 3. Giao diện Cửa hàng Khách hàng tiêu chuẩn
  return (
    <div className="min-h-screen flex flex-col justify-between bg-background text-text-ink-black">
      <Header />
      <div className="flex-1">{children}</div>
      <Footer />
      <CartDrawer />
      <AiChatWidget />
    </div>
  );
}

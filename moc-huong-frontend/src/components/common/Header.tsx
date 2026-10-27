'use client';

import React, { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';
import { useCartStore } from '@/store/useCartStore';
import { useWishlistStore } from '@/store/useWishlistStore';
import toast from 'react-hot-toast';

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, role, logout } = useAuthStore();
  const { getTotalItems, openCart, fetchCart } = useCartStore();
  const { wishlistIds, fetchWishlist } = useWishlistStore();

  const [searchQuery, setSearchQuery] = useState('');
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);

  const totalCartCount = getTotalItems();
  const totalWishlistCount = wishlistIds.length;

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
      fetchWishlist();
    }
  }, [isAuthenticated, fetchCart, fetchWishlist]);

  // Click outside listener for dropdown
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setIsUserMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(`/shop?keyword=${encodeURIComponent(searchQuery.trim())}`);
      setIsMobileMenuOpen(false);
    }
  };

  const handleLogout = async () => {
    setIsUserMenuOpen(false);
    await logout();
    toast.success('Đã đăng xuất thành công');
    router.push('/');
  };

  const navLinks = [
    { label: 'Trang Chủ', href: '/' },
    { label: 'Cửa Hàng', href: '/shop' },
    { label: 'Nhang Trầm & Nụ', href: '/shop?categoryId=1' },
    { label: 'Tinh Dầu Thiên Nhiên', href: '/shop?categoryId=2' },
    { label: 'Vòng Gỗ & Chuỗi 108', href: '/shop?categoryId=3' },
    { label: 'Về Mộc Hương', href: '/about' },
  ];

  return (
    <>
      {/* Top Announcement Bar */}
      <div className="bg-bg-agarwood-dark text-surface-bright py-2 px-margin-mobile lg:px-margin text-center text-caption-meta font-caption-meta tracking-wider">
        <div className="max-w-[1360px] mx-auto flex items-center justify-center gap-space-sm">
          <span className="opacity-90 flex items-center gap-1.5 flex-wrap justify-center">
            🌿 Miễn phí vận chuyển cho đơn hàng từ 500.000đ | Nhập mã{' '}
            <span
              onClick={() => {
                navigator.clipboard.writeText('MOCHUONG10');
                toast.success('Đã sao chép mã MOCHUONG10!');
              }}
              className="text-secondary-fixed font-semibold tracking-widest cursor-pointer underline hover:text-secondary-fixed-dim"
              title="Bấm để sao chép mã"
            >
              MOCHUONG10
            </span>{' '}
            giảm ngay 10% cho khách hàng mới
          </span>
        </div>
      </div>

      {/* Main Sticky Header */}
      <header className="sticky top-0 z-40 bg-surface/95 backdrop-blur-xl border-b border-border-bamboo/60 shadow-[0_1px_8px_rgba(0,0,0,0.04)]">
        <div className="h-20 max-w-[1360px] mx-auto px-margin-mobile lg:px-margin flex items-center justify-between gap-space-md">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-space-sm group">
            <div className="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-secondary-fixed shadow-md group-hover:scale-105 transition-transform">
              <span className="material-symbols-outlined text-[24px]">spa</span>
            </div>
            <div className="flex flex-col">
              <span className="font-headline-sm text-headline-sm text-primary tracking-wide leading-none font-serif">
                MỘC HƯƠNG
              </span>
              <span className="font-caption-meta text-caption-meta text-text-wood-muted tracking-widest uppercase mt-0.5 text-[11px]">
                Hương Từ Tâm • Mộc Từ Đất
              </span>
            </div>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden xl:flex items-center gap-space-md">
            {navLinks.map((link) => {
              const isActive = pathname === link.href;
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`font-label-md text-label-md transition-colors py-1 ${
                    isActive
                      ? 'text-primary font-bold border-b-2 border-primary'
                      : 'text-on-surface-variant hover:text-primary'
                  }`}
                >
                  {link.label}
                </Link>
              );
            })}
          </nav>

          {/* Right Action Bar */}
          <div className="flex items-center gap-space-sm">
            {/* Search Bar */}
            <form onSubmit={handleSearchSubmit} className="hidden lg:flex items-center bg-surface-container px-space-sm py-1.5 rounded-full border border-border-bamboo/50 focus-within:border-primary transition-colors">
              <span className="material-symbols-outlined text-text-wood-muted text-[18px] mr-2">search</span>
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Tìm nhang trầm, tinh dầu..."
                className="bg-transparent font-body-sm text-body-sm text-text-ink-black placeholder:text-text-wood-muted focus:outline-none w-44 xl:w-52"
              />
            </form>

            {/* Wishlist Button */}
            <Link
              href="/account/wishlist"
              aria-label="Danh sách yêu thích"
              className="relative p-2 rounded-full text-on-surface-variant hover:bg-surface-container-high hover:text-primary transition-colors"
            >
              <span className="material-symbols-outlined text-[22px]">favorite</span>
              {totalWishlistCount > 0 && (
                <span className="absolute top-1 right-1 w-4 h-4 bg-status-cinnabar text-surface-container-lowest text-[10px] font-semibold flex items-center justify-center rounded-full">
                  {totalWishlistCount}
                </span>
              )}
            </Link>

            {/* Shopping Cart Button */}
            <button
              onClick={openCart}
              aria-label="Giỏ hàng"
              className="relative p-2 rounded-full text-on-surface-variant hover:bg-surface-container-high hover:text-primary transition-colors"
            >
              <span className="material-symbols-outlined text-[22px]">shopping_bag</span>
              {totalCartCount > 0 && (
                <span className="absolute top-1 right-1 w-4 h-4 bg-primary text-secondary-fixed text-[10px] font-semibold flex items-center justify-center rounded-full">
                  {totalCartCount}
                </span>
              )}
            </button>

            {/* User Dropdown / Auth Menu */}
            <div className="relative" ref={userMenuRef}>
              {isAuthenticated && user ? (
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center gap-1.5 p-1 rounded-full hover:bg-surface-container-high transition-colors focus:outline-none"
                >
                  <div className="w-8 h-8 rounded-full bg-primary-container text-secondary-fixed flex items-center justify-center font-bold text-sm overflow-hidden border border-border-bamboo">
                    {user.avatarUrl ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={user.avatarUrl} alt={user.fullName} className="w-full h-full object-cover" />
                    ) : (
                      user.fullName?.charAt(0) || 'M'
                    )}
                  </div>
                  <span className="material-symbols-outlined text-text-wood-muted text-[16px]">expand_more</span>
                </button>
              ) : (
                <div className="hidden sm:flex items-center gap-2 pl-2">
                  <Link
                    href="/login"
                    className="font-label-md text-label-md text-primary font-medium px-3 py-1.5 rounded-lg hover:bg-surface-container transition-colors"
                  >
                    Đăng Nhập
                  </Link>
                  <Link
                    href="/register"
                    className="font-label-md text-label-md bg-primary text-secondary-fixed px-3.5 py-1.5 rounded-lg hover:bg-primary-container transition-colors font-medium shadow-sm"
                  >
                    Đăng Ký
                  </Link>
                </div>
              )}

              {/* User Dropdown Content */}
              {isUserMenuOpen && isAuthenticated && user && (
                <div className="absolute right-0 mt-2 w-64 bg-surface rounded-xl shadow-xl border border-border-bamboo py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-150">
                  <div className="px-4 py-3 border-b border-border-bamboo/60">
                    <p className="font-label-lg font-bold text-text-ink-black truncate">{user.fullName}</p>
                    <p className="font-caption-meta text-text-wood-muted truncate">{user.email}</p>
                    <div className="mt-1.5 flex items-center gap-2">
                      <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-primary text-secondary-fixed">
                        {role || 'CUSTOMER'}
                      </span>
                    </div>
                  </div>

                  <div className="py-1">
                    <Link
                      href="/account/profile"
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-text-ink-black hover:bg-surface-container transition-colors"
                    >
                      <span className="material-symbols-outlined text-[18px] text-text-wood-muted">person</span>
                      Hồ Sơ Cá Nhân
                    </Link>
                    <Link
                      href="/account/orders"
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-text-ink-black hover:bg-surface-container transition-colors"
                    >
                      <span className="material-symbols-outlined text-[18px] text-text-wood-muted">receipt_long</span>
                      Lịch Sử Đơn Hàng
                    </Link>
                    <Link
                      href="/account/addresses"
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-text-ink-black hover:bg-surface-container transition-colors"
                    >
                      <span className="material-symbols-outlined text-[18px] text-text-wood-muted">home_pin</span>
                      Sổ Địa Chỉ
                    </Link>
                    <Link
                      href="/account/wishlist"
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-text-ink-black hover:bg-surface-container transition-colors"
                    >
                      <span className="material-symbols-outlined text-[18px] text-text-wood-muted">favorite</span>
                      Danh Sách Yêu Thích
                    </Link>

                    {(role === 'STAFF' || role === 'ADMIN') && (
                      <Link
                        href="/admin"
                        onClick={() => setIsUserMenuOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-secondary font-semibold hover:bg-secondary-container/20 transition-colors border-t border-border-bamboo/40 mt-1"
                      >
                        <span className="material-symbols-outlined text-[18px] text-secondary">admin_panel_settings</span>
                        Cổng Quản Trị ({role})
                      </Link>
                    )}
                  </div>

                  <div className="border-t border-border-bamboo/60 pt-1">
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-4 py-2 text-sm text-status-cinnabar hover:bg-status-cinnabar/10 transition-colors text-left"
                    >
                      <span className="material-symbols-outlined text-[18px]">logout</span>
                      Đăng Xuất
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Mobile Menu Toggle Button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="xl:hidden p-2 rounded-lg text-primary hover:bg-surface-container transition-colors"
              aria-label="Menu"
            >
              <span className="material-symbols-outlined text-[26px]">
                {isMobileMenuOpen ? 'close' : 'menu'}
              </span>
            </button>
          </div>
        </div>

        {/* Mobile Navigation Drawer */}
        {isMobileMenuOpen && (
          <div className="xl:hidden bg-surface border-b border-border-bamboo px-6 py-4 flex flex-col gap-3 animate-in fade-in slide-in-from-top-2 duration-150">
            <form onSubmit={handleSearchSubmit} className="flex items-center bg-surface-container px-3 py-2 rounded-lg border border-border-bamboo">
              <span className="material-symbols-outlined text-text-wood-muted text-[18px] mr-2">search</span>
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Tìm nhang trầm, tinh dầu..."
                className="bg-transparent font-body-sm text-body-sm text-text-ink-black focus:outline-none w-full"
              />
            </form>

            <div className="flex flex-col gap-1 pt-2">
              {navLinks.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="px-3 py-2 text-text-ink-black font-medium hover:bg-surface-container rounded-lg transition-colors"
                >
                  {link.label}
                </Link>
              ))}

              {!isAuthenticated && (
                <div className="flex gap-2 pt-3 border-t border-border-bamboo/60">
                  <Link
                    href="/login"
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="flex-1 text-center py-2 border border-primary text-primary font-medium rounded-lg"
                  >
                    Đăng Nhập
                  </Link>
                  <Link
                    href="/register"
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="flex-1 text-center py-2 bg-primary text-secondary-fixed font-medium rounded-lg"
                  >
                    Đăng Ký
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}
      </header>
    </>
  );
}

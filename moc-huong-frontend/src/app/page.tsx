'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { catalogService } from '@/services/catalogService';
import { orderService } from '@/services/orderService';
import { CouponResponse, ProductResponse } from '@/types';
import ProductCard from '@/components/product/ProductCard';
import toast from 'react-hot-toast';

// Fallback featured products for graceful offline / initial empty state
const FALLBACK_FEATURED: ProductResponse[] = [
  {
    id: 1,
    categoryId: 1,
    name: 'Nhang Trầm Hương Rừng Tự Nhiên Cao Cấp (Hộp 40 Nén)',
    slug: 'nhang-tram-huong-rung-tu-nhien',
    skuPrefix: 'NHANG-TRAM',
    description: 'Nén nhang trầm hương rừng nguyên bản từ Tiên Phước Quảng Nam, mùi hương ngọt sâu thanh tịnh.',
    brand: 'Mộc Hương',
    basePrice: 380000,
    startingPrice: 380000,
    tags: ['Best Seller', 'Trầm Rừng'],
    isActive: true,
    averageRating: 5.0,
    reviewCount: 38,
    totalSold: 1250,
    images: [
      {
        id: 1,
        productId: 1,
        imageUrl: 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=800',
        displayOrder: 1,
        isPrimary: true,
      },
    ],
    variants: [
      {
        id: 1,
        productId: 1,
        sku: 'NHANG-TRAM-40',
        variantName: 'Hộp 40 nén (20cm)',
        price: 380000,
        weightGrams: 150,
        isActive: true,
        availableStock: 50,
      },
    ],
  },
  {
    id: 2,
    categoryId: 1,
    name: 'Nụ Trầm Hương Thác Khói Phong Thủy Thượng Hạng',
    slug: 'nu-tram-huong-thac-khoi',
    skuPrefix: 'NU-TRAM',
    description: 'Trầm nụ chảy ngược tạo dòng thác khói bồng bềnh thanh tẩy uế khí, an định tinh thần.',
    brand: 'Mộc Hương',
    basePrice: 290000,
    startingPrice: 290000,
    tags: ['Thác Khói', 'Bán Chạy'],
    isActive: true,
    averageRating: 4.9,
    reviewCount: 42,
    totalSold: 980,
    images: [
      {
        id: 2,
        productId: 2,
        imageUrl: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800',
        displayOrder: 1,
        isPrimary: true,
      },
    ],
    variants: [
      {
        id: 2,
        productId: 2,
        sku: 'NU-TRAM-50',
        variantName: 'Hộp 50 nụ',
        price: 290000,
        weightGrams: 200,
        isActive: true,
        availableStock: 65,
      },
    ],
  },
  {
    id: 3,
    categoryId: 3,
    name: 'Vòng Tay Trầm Hương Tốc Kiến Tự Nhiên 108 Hạt',
    slug: 'vong-tay-tram-huong-108-hat',
    skuPrefix: 'VONG-108',
    description: 'Chuỗi hạt thiền định 108 hạt quấn 4 vòng tay, mùi thơm dịu vĩnh cửu tích tụ vạn niên.',
    brand: 'Mộc Hương',
    basePrice: 1450000,
    startingPrice: 1450000,
    tags: ['Trầm Tốc', 'Phong Thủy'],
    isActive: true,
    averageRating: 5.0,
    reviewCount: 64,
    totalSold: 420,
    images: [
      {
        id: 3,
        productId: 3,
        imageUrl: 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=800',
        displayOrder: 1,
        isPrimary: true,
      },
    ],
    variants: [
      {
        id: 3,
        productId: 3,
        sku: 'VONG-108-6MM',
        variantName: 'Hạt 6mm (Nữ/Nam cổ tay nhỏ)',
        price: 1450000,
        weightGrams: 50,
        isActive: true,
        availableStock: 18,
      },
    ],
  },
  {
    id: 4,
    categoryId: 2,
    name: 'Tinh Dầu Trầm Hương Kỳ Nam Chưng Cất Tự Nhiên 10ml',
    slug: 'tinh-dau-tram-huong-ky-nam',
    skuPrefix: 'TD-TRAM',
    description: 'Tinh dầu nguyên chất ép lạnh và chưng cất hơi nước, hương thơm thanh tao lưu luyến.',
    brand: 'Mộc Hương',
    basePrice: 650000,
    startingPrice: 650000,
    tags: ['Nguyên Chất', 'Thiền Định'],
    isActive: true,
    averageRating: 4.8,
    reviewCount: 29,
    totalSold: 310,
    images: [
      {
        id: 4,
        productId: 4,
        imageUrl: 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=800',
        displayOrder: 1,
        isPrimary: true,
      },
    ],
    variants: [
      {
        id: 4,
        productId: 4,
        sku: 'TD-TRAM-10ML',
        variantName: 'Lọ 10ml giọt nhỏ',
        price: 650000,
        weightGrams: 30,
        isActive: true,
        availableStock: 40,
      },
    ],
  },
];

export default function HomePage() {
  const [featuredProducts, setFeaturedProducts] = useState<ProductResponse[]>(FALLBACK_FEATURED);
  const [activeCoupons, setActiveCoupons] = useState<CouponResponse[]>([]);
  const [activeCategoryFilter, setActiveCategoryFilter] = useState<string>('all');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    async function loadData() {
      setIsLoading(true);
      try {
        const [products, coupons] = await Promise.allSettled([
          catalogService.getFeaturedProducts(),
          orderService.getActiveCoupons(),
        ]);

        if (products.status === 'fulfilled' && products.value.length > 0) {
          setFeaturedProducts(products.value);
        }
        if (coupons.status === 'fulfilled' && coupons.value.length > 0) {
          setActiveCoupons(coupons.value);
        }
      } catch (e) {
        console.warn('Backend featured products not yet populated, using fallback catalog', e);
      } finally {
        setIsLoading(false);
      }
    }
    loadData();
  }, []);

  const handleCopyCode = (code: string) => {
    navigator.clipboard.writeText(code);
    toast.success(`Đã sao chép mã ưu đãi ${code}!`, {
      icon: '🏷️',
      style: {
        background: '#F2ECE1',
        color: '#231C18',
        border: '1px solid #C89B57',
      },
    });
  };

  const filteredProducts =
    activeCategoryFilter === 'all'
      ? featuredProducts
      : featuredProducts.filter((p) => {
          if (activeCategoryFilter === 'nhang') return p.categoryId === 1;
          if (activeCategoryFilter === 'tinhdau') return p.categoryId === 2;
          if (activeCategoryFilter === 'vonggo') return p.categoryId === 3;
          return true;
        });

  return (
    <main className="w-full bg-background">
      {/* 1. EDITORIAL HERO BANNER */}
      <section className="relative bg-bg-agarwood-dark text-surface-bright overflow-hidden">
        <div className="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-secondary/15 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-40 right-10 w-[500px] h-[500px] rounded-full bg-tertiary-container/30 blur-3xl pointer-events-none" />

        <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin py-space-xl lg:py-24 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-space-xl items-center">
            {/* Left Content Column */}
            <div className="lg:col-span-7 flex flex-col gap-space-md">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-surface-container-highest/10 w-fit border border-secondary-fixed/20">
                <span className="w-2 h-2 rounded-full bg-secondary-fixed animate-pulse" />
                <span className="font-caption-meta text-caption-meta uppercase tracking-widest text-secondary-fixed font-semibold">
                  Mộc Hương Trầm Đạo • Di Sản Thủ Công
                </span>
              </div>

              <h1 className="font-serif text-4xl sm:text-5xl lg:text-6xl text-surface-bright leading-tight font-semibold tracking-normal">
                Tìm Lại Khoảnh Khắc{' '}
                <span className="italic text-secondary-fixed font-normal">Tĩnh Lặng</span> Giữa Đời Thường
              </h1>

              <p className="font-body-lg text-body-lg text-surface-variant max-w-xl leading-relaxed">
                Tinh hoa trầm Việt nguyên bản và vật phẩm thiền định chế tác thủ công từ tâm, lan tỏa năng lượng bình
                yên thuần khiết trong từng hơi thở.
              </p>

              {/* Trust Badges */}
              <div className="flex flex-wrap items-center gap-space-sm pt-space-xs">
                <div className="flex items-center gap-2 bg-surface-container-highest/10 px-3.5 py-1.5 rounded-full border border-border-bamboo/20">
                  <span className="material-symbols-outlined text-secondary-fixed text-[18px]">verified</span>
                  <span className="font-label-md text-surface-dim">100% Trầm Thật Tự Nhiên</span>
                </div>
                <div className="flex items-center gap-2 bg-surface-container-highest/10 px-3.5 py-1.5 rounded-full border border-border-bamboo/20">
                  <span className="material-symbols-outlined text-tertiary-fixed text-[18px]">eco</span>
                  <span className="font-label-md text-surface-dim">Không Hóa Chất Tạo Mùi</span>
                </div>
                <div className="flex items-center gap-2 bg-surface-container-highest/10 px-3.5 py-1.5 rounded-full border border-border-bamboo/20">
                  <span className="material-symbols-outlined text-secondary-fixed text-[18px]">handyman</span>
                  <span className="font-label-md text-surface-dim">Chế Tác Thủ Công</span>
                </div>
              </div>

              {/* CTA Buttons */}
              <div className="flex flex-wrap items-center gap-space-md pt-space-sm">
                <Link
                  href="/shop"
                  className="inline-flex items-center justify-center gap-2 px-space-lg py-3.5 rounded-xl bg-secondary-fixed text-primary font-label-lg font-bold hover:bg-secondary-container transition-all shadow-md active:scale-95"
                >
                  <span>Khám Phá Bộ Sưu Tập</span>
                  <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                </Link>
                <Link
                  href="/about"
                  className="inline-flex items-center justify-center gap-2 px-space-lg py-3.5 rounded-xl bg-surface-container-highest/15 text-surface-bright font-label-lg hover:bg-surface-container-highest/25 transition-all border border-border-bamboo/20"
                >
                  <span className="material-symbols-outlined text-[18px] text-secondary-fixed">self_improvement</span>
                  <span>Chọn Hương Hợp Mệnh</span>
                </Link>
              </div>

              {/* 3 Key Metrics */}
              <div className="grid grid-cols-3 gap-space-md pt-space-md mt-space-sm bg-surface-container-highest/5 p-space-md rounded-2xl border border-border-bamboo/10">
                <div>
                  <div className="font-serif text-3xl text-secondary-fixed font-bold">10+</div>
                  <p className="font-caption-meta text-surface-variant uppercase tracking-wider text-[11px] mt-0.5">
                    Năm Chế Tác Trầm
                  </p>
                </div>
                <div>
                  <div className="font-serif text-3xl text-secondary-fixed font-bold">100%</div>
                  <p className="font-caption-meta text-surface-variant uppercase tracking-wider text-[11px] mt-0.5">
                    Nguyên Liệu Sạch
                  </p>
                </div>
                <div>
                  <div className="font-serif text-3xl text-secondary-fixed font-bold">15k+</div>
                  <p className="font-caption-meta text-surface-variant uppercase tracking-wider text-[11px] mt-0.5">
                    Khách Hàng An Tâm
                  </p>
                </div>
              </div>
            </div>

            {/* Right Visual Showcase */}
            <div className="lg:col-span-5 relative flex justify-center">
              <div className="relative w-full max-w-md">
                {/* Indochine Arch Visual Frame */}
                <div className="overflow-hidden rounded-t-[180px] rounded-b-2xl bg-surface-container-highest/10 shadow-2xl relative border-2 border-secondary-fixed/20">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    className="w-full h-[520px] object-cover scale-100 hover:scale-105 transition-transform duration-700 ease-out"
                    alt="Lư xông trầm và vòng gỗ thiền định Mộc Hương"
                    src="https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-bg-agarwood-dark/85 via-transparent to-transparent pointer-events-none" />
                  <div className="absolute bottom-6 left-6 right-6 p-4 rounded-xl bg-bg-agarwood-dark/80 backdrop-blur-md border border-border-bamboo/20">
                    <div className="flex items-center justify-between">
                      <div className="flex flex-col">
                        <span className="font-caption-meta text-secondary-fixed uppercase tracking-wider text-[11px]">
                          Bộ Thưởng Trầm Thiền Định
                        </span>
                        <span className="font-serif text-lg font-bold text-surface-bright">
                          Trầm Tốc & Chuỗi 108 Hạt
                        </span>
                      </div>
                      <div className="w-10 h-10 rounded-full bg-secondary-container/20 flex items-center justify-center text-secondary-fixed">
                        <span className="material-symbols-outlined text-[20px]">spa</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Floating Note Badge */}
                <div className="hidden sm:flex absolute -bottom-5 -left-6 bg-bg-warm-sand text-text-ink-black px-4 py-3 rounded-2xl shadow-xl items-center gap-3 border border-border-bamboo">
                  <span className="material-symbols-outlined text-secondary text-[26px]">workspace_premium</span>
                  <div>
                    <p className="font-label-md font-bold text-primary">Trầm Hương Tiên Phước</p>
                    <p className="font-caption-meta text-text-wood-muted text-[11px]">Tuyển chọn vân dầu tự nhiên</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. CATEGORY TREE SHOWCASE (NGŨ HƯƠNG AN ĐỊNH) */}
      <section className="py-space-xl lg:py-24 bg-background" id="danh-muc">
        <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
          <div className="flex flex-col md:flex-row md:items-end justify-between mb-space-xl gap-4">
            <div>
              <div className="inline-flex items-center gap-2 mb-1">
                <span className="material-symbols-outlined text-secondary text-[20px]">spa</span>
                <span className="font-caption-meta uppercase tracking-widest text-secondary font-semibold">
                  Ngũ Trụ Hương Đạo
                </span>
              </div>
              <h2 className="font-serif text-3xl lg:text-4xl text-primary font-bold">
                Bộ Sưu Tập Phẩm Hương Tĩnh Tâm
              </h2>
            </div>
            <Link
              href="/shop"
              className="inline-flex items-center gap-1.5 text-secondary hover:text-primary font-label-md font-semibold transition-colors group"
            >
              <span>Xem Tất Cả Danh Mục</span>
              <span className="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">
                arrow_forward
              </span>
            </Link>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-space-md">
            {[
              {
                title: 'Nhang Trầm Tự Nhiên',
                count: '18 Phẩm vị',
                href: '/shop?categoryId=1',
                image: 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=500',
              },
              {
                title: 'Trầm Nụ & Thác Khói',
                count: '12 Mẫu thác',
                href: '/shop?categoryId=1',
                image: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=500',
              },
              {
                title: 'Tinh Dầu Thiên Nhiên',
                count: '15 Hương hoa cỏ',
                href: '/shop?categoryId=2',
                image: 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=500',
              },
              {
                title: 'Vòng Gỗ Phong Thủy',
                count: '24 Dáng hạt',
                href: '/shop?categoryId=3',
                image: 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=500',
              },
              {
                title: 'Chuỗi Hạt Thiền 108',
                count: '10 Mẫu niệm châu',
                href: '/shop?categoryId=3',
                image: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=500',
              },
            ].map((cat, idx) => (
              <Link
                key={idx}
                href={cat.href}
                className="group relative bg-bg-warm-sand rounded-2xl overflow-hidden border border-border-bamboo/60 hover:border-secondary transition-all duration-300 hover:shadow-lg flex flex-col"
              >
                <div className="relative aspect-[4/5] overflow-hidden bg-surface-container">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={cat.image}
                    alt={cat.title}
                    className="w-full h-full object-cover group-hover:scale-108 transition-transform duration-700 ease-out"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-bg-agarwood-dark/75 via-transparent to-transparent pointer-events-none" />
                  <div className="absolute bottom-3 left-3 right-3 text-surface-bright">
                    <h3 className="font-serif font-bold text-sm sm:text-base leading-tight group-hover:text-secondary-fixed transition-colors">
                      {cat.title}
                    </h3>
                    <span className="font-caption-meta text-surface-variant text-[11px]">{cat.count}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* 3. ACTIVE COUPONS BANNER */}
      {activeCoupons.length > 0 && (
        <section className="bg-bg-warm-sand/80 py-8 border-y border-border-bamboo">
          <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
            <div className="flex items-center gap-2 mb-4">
              <span className="material-symbols-outlined text-status-cinnabar text-[22px]">loyalty</span>
              <h3 className="font-serif text-xl font-bold text-primary">Ưu Đãi Đang Mở Dành Cho Quý Khách</h3>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {activeCoupons.map((coupon) => (
                <div
                  key={coupon.id}
                  className="bg-surface p-4 rounded-xl border border-dashed border-secondary/60 flex items-center justify-between shadow-sm"
                >
                  <div>
                    <span className="font-mono font-bold text-primary text-base tracking-wider bg-secondary-fixed/30 px-2 py-0.5 rounded">
                      {coupon.code}
                    </span>
                    <p className="font-label-md font-semibold text-text-ink-black text-sm mt-1">{coupon.title}</p>
                    <p className="font-caption-meta text-text-wood-muted text-xs">
                      {coupon.description || `Đơn tối thiểu ${coupon.minOrderValue.toLocaleString('vi-VN')} ₫`}
                    </p>
                  </div>
                  <button
                    onClick={() => handleCopyCode(coupon.code)}
                    className="px-3 py-1.5 rounded-lg bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm"
                  >
                    Lưu Mã
                  </button>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* 4. FEATURED PRODUCTS SECTION */}
      <section className="py-space-xl lg:py-24 bg-surface-container-low/50">
        <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
          <div className="flex flex-col md:flex-row md:items-end justify-between mb-space-xl gap-4">
            <div>
              <div className="inline-flex items-center gap-2 mb-1">
                <span className="material-symbols-outlined text-secondary text-[20px]">recommend</span>
                <span className="font-caption-meta uppercase tracking-widest text-secondary font-semibold">
                  Tuyển Chọn Tâm Huyết
                </span>
              </div>
              <h2 className="font-serif text-3xl lg:text-4xl text-primary font-bold">
                Vật Phẩm Nổi Bật & Được Yêu Thích
              </h2>
            </div>

            {/* Filter Tabs */}
            <div className="flex items-center gap-2 overflow-x-auto pb-1">
              {[
                { id: 'all', label: 'Tất Cả Phẩm Hương' },
                { id: 'nhang', label: 'Nhang Trầm & Nụ' },
                { id: 'tinhdau', label: 'Tinh Dầu Thiên Nhiên' },
                { id: 'vonggo', label: 'Vòng Gỗ & Chuỗi 108' },
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveCategoryFilter(tab.id)}
                  className={`px-4 py-2 rounded-xl font-label-md text-xs sm:text-sm font-semibold transition-all whitespace-nowrap ${
                    activeCategoryFilter === tab.id
                      ? 'bg-primary text-secondary-fixed shadow-md'
                      : 'bg-surface text-text-wood-muted hover:text-text-ink-black hover:bg-surface-container border border-border-bamboo/60'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {/* Grid */}
          {isLoading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-space-lg">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="h-96 rounded-2xl bg-surface-container animate-pulse border border-border-bamboo/40" />
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-space-lg">
              {filteredProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}

          <div className="text-center mt-space-xl">
            <Link
              href="/shop"
              className="inline-flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl bg-surface border-2 border-primary text-primary font-label-lg font-bold hover:bg-primary hover:text-secondary-fixed transition-all shadow-sm"
            >
              <span>Xem Thêm Hơn 50+ Phẩm Hương Khác</span>
              <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
            </Link>
          </div>
        </div>
      </section>

      {/* 5. WHY CHOOSE US / TRIẾT LÝ MỘC HƯƠNG */}
      <section className="py-space-xl lg:py-24 bg-background">
        <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
          <div className="text-center max-w-2xl mx-auto mb-space-xl">
            <span className="font-caption-meta text-caption-meta text-secondary uppercase tracking-widest font-semibold">
              Cam Kết Từ Tâm
            </span>
            <h2 className="font-serif text-3xl lg:text-4xl text-primary font-bold mt-1">
              3 Chuẩn Mực Vàng Của Mộc Hương
            </h2>
            <p className="font-body-base text-text-wood-muted mt-3">
              Mỗi nén nhang thắp lên hay chuỗi hạt đeo trên tay đều là tấm lòng gửi trọn bình an đến quý bạn hữu.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-space-lg">
            <div className="bg-bg-warm-sand p-8 rounded-2xl flex flex-col items-center text-center border border-border-bamboo/60 shadow-sm">
              <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-primary mb-5 shadow-inner">
                <span className="material-symbols-outlined text-[32px]">forest</span>
              </div>
              <h3 className="font-serif text-xl font-bold text-primary mb-2">Trầm Rừng Thuần Khiết</h3>
              <p className="font-body-base text-text-wood-muted text-sm leading-relaxed">
                Tuyển chọn từ những cây Dó Bầu tạo trầm tự nhiên tại vùng đất Tiên Phước, tích tụ linh khí đất trời qua
                nhiều thập kỷ.
              </p>
            </div>

            <div className="bg-bg-warm-sand p-8 rounded-2xl flex flex-col items-center text-center border border-border-bamboo/60 shadow-sm">
              <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-primary mb-5 shadow-inner">
                <span className="material-symbols-outlined text-[32px]">handyman</span>
              </div>
              <h3 className="font-serif text-xl font-bold text-primary mb-2">Chế Tác Thủ Công</h3>
              <p className="font-body-base text-text-wood-muted text-sm leading-relaxed">
                Nghệ nhân gia truyền mài giũa từng viên chuỗi, se từng nén nhang thủ công với chất kết dính từ vỏ cây Bời
                Lời tự nhiên.
              </p>
            </div>

            <div className="bg-bg-warm-sand p-8 rounded-2xl flex flex-col items-center text-center border border-border-bamboo/60 shadow-sm">
              <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-primary mb-5 shadow-inner">
                <span className="material-symbols-outlined text-[32px]">spa</span>
              </div>
              <h3 className="font-serif text-xl font-bold text-primary mb-2">Năng Lượng An Lành</h3>
              <p className="font-body-base text-text-wood-muted text-sm leading-relaxed">
                Tuyệt đối không pha trộn bột keo hóa chất độc hại hay hương liệu tổng hợp. Làn khói sạch thuần khiết, an
                toàn cho sức khỏe.
              </p>
            </div>
          </div>

          {/* Workshop Mini Banner */}
          <div className="mt-space-xl bg-bg-agarwood-dark text-surface-bright rounded-3xl p-8 lg:p-12 flex flex-col lg:flex-row items-center justify-between gap-6 relative overflow-hidden border border-border-bamboo/20">
            <div className="flex flex-col gap-2 max-w-xl">
              <span className="font-caption-meta text-secondary-fixed uppercase tracking-wider text-xs font-semibold">
                Trải Nghiệm Thực Tế Tại Showroom
              </span>
              <h3 className="font-serif text-2xl lg:text-3xl text-surface-bright font-bold">
                Thưởng Trầm Đạo & Uống Trà Shan Tuyết Cổ Thụ
              </h3>
              <p className="font-body-sm text-surface-variant leading-relaxed text-sm">
                Kính mời quý bạn hữu ghé thăm không gian Mộc Hương để tự tay thưởng thức hương trầm mộc và chiêm ngưỡng
                các tác phẩm trầm cảnh độc bản.
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <Link
                href="/about"
                className="px-6 py-3 rounded-xl bg-secondary-fixed text-primary font-label-lg font-bold hover:bg-secondary-container transition-colors shadow-md"
              >
                Đặt Lịch Thưởng Trầm
              </Link>
              <Link
                href="/about"
                className="px-6 py-3 rounded-xl bg-surface-container-highest/20 text-surface-bright font-label-lg hover:bg-surface-container-highest/30 transition-colors border border-border-bamboo/20"
              >
                Tìm Showroom Gần Nhất
              </Link>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

'use client';

import React, { useState, useEffect, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { catalogService } from '@/services/catalogService';
import { CategoryResponse, ProductResponse, Page } from '@/types';
import ProductCard from '@/components/product/ProductCard';

const PRICE_RANGES = [
  { label: 'Tất cả mức giá', min: undefined, max: undefined },
  { label: 'Dưới 300.000 ₫', min: 0, max: 300000 },
  { label: '300.000 ₫ - 800.000 ₫', min: 300000, max: 800000 },
  { label: '800.000 ₫ - 2.000.000 ₫', min: 800000, max: 2000000 },
  { label: 'Trên 2.000.000 ₫', min: 2000000, max: undefined },
];

function ShopContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const initialKeyword = searchParams.get('keyword') || '';
  const initialCategory = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined;

  const [keyword, setKeyword] = useState(initialKeyword);
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>(initialCategory);
  const [selectedPriceIdx, setSelectedPriceIdx] = useState<number>(0);
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [direction, setDirection] = useState<string>('desc');
  const [currentPage, setCurrentPage] = useState<number>(0);

  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [productsData, setProductsData] = useState<Page<ProductResponse> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Load Categories
  useEffect(() => {
    async function loadCategories() {
      try {
        const cats = await catalogService.getCategories();
        setCategories(cats);
      } catch (e) {
        console.warn('Could not load categories:', e);
      }
    }
    loadCategories();
  }, []);

  // Update state when search params change
  useEffect(() => {
    const kw = searchParams.get('keyword');
    const catId = searchParams.get('categoryId');
    if (kw !== null) setKeyword(kw);
    if (catId !== null) setSelectedCategory(Number(catId));
  }, [searchParams]);

  // Load Products with filters
  useEffect(() => {
    async function loadProducts() {
      setIsLoading(true);
      try {
        const priceConfig = PRICE_RANGES[selectedPriceIdx];
        const res = await catalogService.getProducts({
          keyword: keyword.trim() || undefined,
          categoryId: selectedCategory,
          minPrice: priceConfig.min,
          maxPrice: priceConfig.max,
          page: currentPage,
          size: 12,
          sortBy,
          direction,
        });
        setProductsData(res);
      } catch (e) {
        console.warn('Failed to load products from API, falling back to empty page', e);
        setProductsData(null);
      } finally {
        setIsLoading(false);
      }
    }
    loadProducts();
  }, [keyword, selectedCategory, selectedPriceIdx, sortBy, direction, currentPage]);

  const handleClearFilters = () => {
    setKeyword('');
    setSelectedCategory(undefined);
    setSelectedPriceIdx(0);
    setSortBy('createdAt');
    setDirection('desc');
    setCurrentPage(0);
    router.push('/shop');
  };

  const handleSortChange = (value: string) => {
    if (value === 'price_asc') {
      setSortBy('basePrice');
      setDirection('asc');
    } else if (value === 'price_desc') {
      setSortBy('basePrice');
      setDirection('desc');
    } else {
      setSortBy('createdAt');
      setDirection('desc');
    }
  };

  const totalItems = productsData?.totalElements || 0;
  const totalPages = productsData?.totalPages || 1;
  const products = productsData?.content || [];

  return (
    <div className="w-full bg-background min-h-screen">
      {/* Category Architectural Banner */}
      <section className="w-full bg-surface-container-low border-b border-border-bamboo/60 relative overflow-hidden py-8 lg:py-12">
        <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin relative z-10">
          <nav aria-label="Đường dẫn" className="flex items-center gap-2 font-caption-meta text-xs text-text-wood-muted mb-3">
            <Link href="/" className="hover:text-primary transition-colors">Trang chủ</Link>
            <span className="text-outline-variant font-mono">/</span>
            <span className="text-primary font-semibold">Cửa Hàng Mộc Hương</span>
          </nav>

          <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
            <div>
              <div className="inline-flex items-center gap-2 mb-1.5">
                <span className="w-2 h-2 rounded-full bg-primary" />
                <span className="font-caption-meta text-xs uppercase tracking-widest text-text-wood-muted font-semibold">
                  Tuyển Tập Thượng Phẩm Hương & Mộc
                </span>
              </div>
              <h1 className="font-serif text-3xl lg:text-4xl text-primary font-bold">
                Cửa Hàng Không Gian Thiền
              </h1>
              <p className="font-body-base text-text-wood-muted italic text-sm mt-1 max-w-xl">
                “Những sản phẩm tích tụ linh khí đất trời qua nhiều thập kỷ, mang lại bình an, tĩnh tại cho tâm hồn.”
              </p>
            </div>

            <div className="flex items-center gap-2 self-start lg:self-end bg-surface px-4 py-2 rounded-full border border-border-bamboo shadow-sm">
              <span className="material-symbols-outlined text-[18px] text-secondary">spa</span>
              <span className="font-label-md text-xs text-primary font-medium">
                Tìm thấy <strong className="font-bold text-secondary">{totalItems}</strong> phẩm vị
              </span>
            </div>
          </div>
        </div>
      </section>

      {/* Main Catalog Space: 12-Column Grid */}
      <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin py-8 lg:py-12">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* ================= LEFT SIDEBAR FILTER (3 Cols) ================= */}
          <aside className="lg:col-span-3 flex flex-col gap-6 lg:sticky lg:top-28">
            <div className="bg-bg-warm-sand/80 p-5 lg:p-6 rounded-2xl border border-border-bamboo/80 flex flex-col gap-6 shadow-sm">
              {/* Header */}
              <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
                <div className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary text-[22px]">tune</span>
                  <h2 className="font-serif text-lg text-primary font-bold">Bộ Lọc Tinh Chọn</h2>
                </div>
                <button
                  onClick={handleClearFilters}
                  className="font-caption-meta text-xs text-text-wood-muted hover:text-status-cinnabar underline transition-colors"
                >
                  Xóa tất cả
                </button>
              </div>

              {/* Keyword Search Input */}
              <div>
                <label className="block font-label-md text-xs font-semibold uppercase tracking-wider text-primary mb-1.5">
                  Tìm Kiếm
                </label>
                <div className="relative">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
                    search
                  </span>
                  <input
                    type="text"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    placeholder="Tên, mùi hương, xuất xứ..."
                    className="w-full pl-9 pr-3 py-2 bg-surface rounded-xl border border-border-bamboo font-body-sm text-xs text-text-ink-black focus:outline-none focus:border-primary transition-all"
                  />
                </div>
              </div>

              {/* Category Tree */}
              <div>
                <span className="block font-label-md text-xs font-semibold uppercase tracking-wider text-primary mb-2">
                  Danh Mục Sản Phẩm
                </span>
                <div className="space-y-1">
                  <button
                    onClick={() => setSelectedCategory(undefined)}
                    className={`w-full text-left px-3 py-2 rounded-xl text-xs font-medium transition-all flex items-center justify-between ${
                      selectedCategory === undefined
                        ? 'bg-primary text-secondary-fixed shadow-sm'
                        : 'text-text-wood-muted hover:bg-surface hover:text-primary'
                    }`}
                  >
                    <span>Tất Cả Danh Mục</span>
                  </button>

                  {categories.map((cat) => (
                    <button
                      key={cat.id}
                      onClick={() => setSelectedCategory(cat.id)}
                      className={`w-full text-left px-3 py-2 rounded-xl text-xs font-medium transition-all flex items-center justify-between ${
                        selectedCategory === cat.id
                          ? 'bg-primary text-secondary-fixed shadow-sm'
                          : 'text-text-wood-muted hover:bg-surface hover:text-primary'
                      }`}
                    >
                      <span className="truncate">{cat.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Price Range Filter */}
              <div>
                <span className="block font-label-md text-xs font-semibold uppercase tracking-wider text-primary mb-2">
                  Khoảng Giá
                </span>
                <div className="space-y-1.5">
                  {PRICE_RANGES.map((range, idx) => (
                    <label
                      key={idx}
                      className="flex items-center gap-2 cursor-pointer text-xs text-text-wood-muted hover:text-primary py-0.5"
                    >
                      <input
                        type="radio"
                        name="priceRange"
                        checked={selectedPriceIdx === idx}
                        onChange={() => setSelectedPriceIdx(idx)}
                        className="text-primary focus:ring-primary h-3.5 w-3.5 border-border-bamboo"
                      />
                      <span className={selectedPriceIdx === idx ? 'font-semibold text-primary' : ''}>
                        {range.label}
                      </span>
                    </label>
                  ))}
                </div>
              </div>
            </div>
          </aside>

          {/* ================= RIGHT CATALOG CONTENT (9 Cols) ================= */}
          <main className="lg:col-span-9 flex flex-col gap-6">
            {/* Top Toolbar: Total & Sort */}
            <div className="bg-surface-container-low p-4 rounded-2xl border border-border-bamboo/60 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-sm">
              <span className="text-xs text-text-wood-muted font-medium">
                Hiển thị <strong className="text-primary font-bold">{products.length}</strong> trên{' '}
                <strong className="text-primary font-bold">{totalItems}</strong> sản phẩm
              </span>

              <div className="flex items-center gap-2">
                <span className="text-xs text-text-wood-muted whitespace-nowrap">Sắp xếp theo:</span>
                <select
                  onChange={(e) => handleSortChange(e.target.value)}
                  className="bg-surface border border-border-bamboo rounded-xl px-3 py-1.5 text-xs text-text-ink-black focus:outline-none focus:border-primary font-medium"
                >
                  <option value="newest">Mới nhất</option>
                  <option value="price_asc">Giá: Thấp đến Cao</option>
                  <option value="price_desc">Giá: Cao đến Thấp</option>
                </select>
              </div>
            </div>

            {/* Products Grid */}
            {isLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {[...Array(6)].map((_, i) => (
                  <div
                    key={i}
                    className="h-96 rounded-2xl bg-bg-warm-sand animate-pulse border border-border-bamboo/40"
                  />
                ))}
              </div>
            ) : products.length === 0 ? (
              <div className="bg-bg-warm-sand/50 rounded-2xl p-12 text-center border border-dashed border-border-bamboo flex flex-col items-center">
                <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-text-wood-muted mb-4 border border-border-bamboo">
                  <span className="material-symbols-outlined text-[32px]">filter_alt_off</span>
                </div>
                <h3 className="font-serif text-xl font-bold text-primary mb-2">Chưa tìm thấy sản phẩm phù hợp</h3>
                <p className="font-body-sm text-text-wood-muted max-w-sm mb-6 text-sm">
                  Quý khách vui lòng thử tìm kiếm bằng từ khóa khác hoặc xóa bớt tiêu chí trong bộ lọc.
                </p>
                <button
                  onClick={handleClearFilters}
                  className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm"
                >
                  Xóa Bộ Lọc
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((p) => (
                  <ProductCard key={p.id} product={p} />
                ))}
              </div>
            )}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 pt-6">
                <button
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  className="p-2 rounded-xl border border-border-bamboo bg-surface disabled:opacity-40 hover:bg-surface-container transition-colors"
                  aria-label="Trang trước"
                >
                  <span className="material-symbols-outlined text-[18px]">chevron_left</span>
                </button>

                {[...Array(totalPages)].map((_, idx) => (
                  <button
                    key={idx}
                    onClick={() => setCurrentPage(idx)}
                    className={`w-9 h-9 rounded-xl font-mono text-xs font-semibold transition-all ${
                      currentPage === idx
                        ? 'bg-primary text-secondary-fixed shadow-md'
                        : 'border border-border-bamboo bg-surface text-text-wood-muted hover:bg-surface-container'
                    }`}
                  >
                    {idx + 1}
                  </button>
                ))}

                <button
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                  className="p-2 rounded-xl border border-border-bamboo bg-surface disabled:opacity-40 hover:bg-surface-container transition-colors"
                  aria-label="Trang sau"
                >
                  <span className="material-symbols-outlined text-[18px]">chevron_right</span>
                </button>
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}

export default function ShopPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-background">
          <div className="flex flex-col items-center gap-3">
            <span className="material-symbols-outlined text-[40px] text-primary animate-spin">spa</span>
            <p className="font-serif text-primary text-base">Đang tải không gian hương...</p>
          </div>
        </div>
      }
    >
      <ShopContent />
    </Suspense>
  );
}

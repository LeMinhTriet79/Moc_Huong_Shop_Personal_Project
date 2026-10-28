'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { catalogService } from '@/services/catalogService';
import { InventoryResponse, ProductResponse, ProductVariantResponse, ReviewResponse } from '@/types';
import { formatCurrency, formatDate } from '@/lib/utils';
import { useCartStore } from '@/store/useCartStore';
import { useWishlistStore } from '@/store/useWishlistStore';
import { useAuthStore } from '@/store/useAuthStore';
import toast from 'react-hot-toast';

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = params?.id as string;

  const { addItem } = useCartStore();
  const { isInWishlist, toggleWishlist } = useWishlistStore();
  const { isAuthenticated } = useAuthStore();

  const [product, setProduct] = useState<ProductResponse | null>(null);
  const [selectedVariant, setSelectedVariant] = useState<ProductVariantResponse | null>(null);
  const [selectedImage, setSelectedImage] = useState<string>('');
  const [inventory, setInventory] = useState<InventoryResponse | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [activeTab, setActiveTab] = useState<'details' | 'origin' | 'reviews'>('details');

  // Reviews
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [ratingInput, setRatingInput] = useState<number>(5);
  const [commentInput, setCommentInput] = useState<string>('');
  const [isSubmittingReview, setIsSubmittingReview] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Fetch product and reviews
  useEffect(() => {
    async function fetchProductData() {
      if (!productId) return;
      setIsLoading(true);
      try {
        const prod = await catalogService.getProductById(productId);
        setProduct(prod);

        // Set default variant & primary image
        if (prod.variants && prod.variants.length > 0) {
          const defaultVar = prod.variants[0];
          setSelectedVariant(defaultVar);
          // fetch inventory for default variant
          try {
            const inv = await catalogService.getInventory(defaultVar.id);
            setInventory(inv);
          } catch (e) {
            console.warn('Could not fetch inventory for variant:', e);
          }
        }

        const mainImg =
          prod.images?.find((img) => img.isPrimary)?.imageUrl ||
          prod.images?.[0]?.imageUrl ||
          'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=800';
        setSelectedImage(mainImg);

        // Fetch reviews
        try {
          const revs = await catalogService.getReviews(Number(productId));
          setReviews(revs.content || []);
        } catch (e) {
          console.warn('Could not fetch reviews:', e);
        }
      } catch (error) {
        console.error('Failed to load product:', error);
      } finally {
        setIsLoading(false);
      }
    }
    fetchProductData();
  }, [productId]);

  // Handle variant switch
  const handleVariantChange = async (variant: ProductVariantResponse) => {
    setSelectedVariant(variant);
    try {
      const inv = await catalogService.getInventory(variant.id);
      setInventory(inv);
    } catch (e) {
      console.warn('Failed to fetch inventory:', e);
    }
  };

  const handleAddToCart = async () => {
    if (!product || !selectedVariant) return;
    await addItem({
      productId: product.id,
      variantId: selectedVariant.id,
      sku: selectedVariant.sku,
      productName: product.name,
      variantName: selectedVariant.variantName,
      productImageUrl: selectedImage,
      price: selectedVariant.price,
      quantity,
    });
  };

  const handleBuyNow = async () => {
    await handleAddToCart();
    router.push('/checkout');
  };

  const handleReviewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để gửi đánh giá sản phẩm');
      return;
    }
    if (!commentInput.trim()) {
      toast.error('Vui lòng nhập nội dung đánh giá');
      return;
    }

    setIsSubmittingReview(true);
    try {
      const newReview = await catalogService.createReview({
        productId: Number(productId),
        rating: ratingInput,
        comment: commentInput.trim(),
      });
      setReviews([newReview, ...reviews]);
      setCommentInput('');
      toast.success('Cảm ơn quý khách đã gửi đánh giá cho Mộc Hương!', {
        icon: '🌸',
      });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Có lỗi xảy ra khi gửi đánh giá';
      toast.error(msg);
    } finally {
      setIsSubmittingReview(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <span className="material-symbols-outlined text-[40px] text-primary animate-spin">spa</span>
          <p className="font-serif text-primary text-base">Đang tải không gian chi tiết sản phẩm...</p>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-background px-4 text-center">
        <div className="w-20 h-20 rounded-full bg-bg-warm-sand flex items-center justify-center text-text-wood-muted mb-4 border border-border-bamboo">
          <span className="material-symbols-outlined text-[36px]">inventory_2</span>
        </div>
        <h2 className="font-serif text-2xl font-bold text-primary mb-2">Không tìm thấy sản phẩm</h2>
        <p className="font-body-base text-text-wood-muted max-w-md mb-6">
          Sản phẩm có thể đã ngừng cung cấp hoặc liên kết không chính xác.
        </p>
        <Link
          href="/shop"
          className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-sm font-semibold hover:bg-primary-container transition-colors"
        >
          Trở Lại Cửa Hàng
        </Link>
      </div>
    );
  }

  const isFav = isInWishlist(product.id);
  const currentPrice = selectedVariant?.price || product.basePrice;
  const availableStock = inventory?.availableStock ?? (selectedVariant?.availableStock ?? 10);
  const images = product.images?.length
    ? product.images
    : [{ id: 1, productId: product.id, imageUrl: selectedImage, displayOrder: 1, isPrimary: true }];

  return (
    <div className="w-full bg-background min-h-screen pb-20">
      <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
        {/* 1. Breadcrumbs */}
        <nav aria-label="Đường dẫn" className="pt-6 pb-6 flex items-center flex-wrap gap-2 text-xs font-caption-meta text-text-wood-muted">
          <Link href="/" className="hover:text-primary transition-colors flex items-center gap-1">
            <span className="material-symbols-outlined text-[15px]">home</span>
            <span>Trang chủ</span>
          </Link>
          <span className="text-outline-variant font-mono">/</span>
          <Link href="/shop" className="hover:text-primary transition-colors">
            Cửa Hàng
          </Link>
          <span className="text-outline-variant font-mono">/</span>
          <span className="text-primary font-semibold truncate max-w-xs">{product.name}</span>
        </nav>

        {/* 2. Top 2-Column Product Hero */}
        <section className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start pb-12">
          {/* Left Column: Gallery */}
          <div className="lg:col-span-6 flex flex-col gap-4">
            {/* Main Showcase Image */}
            <div className="relative w-full aspect-square bg-bg-warm-sand rounded-2xl overflow-hidden border border-border-bamboo/70 shadow-sm group">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={selectedImage}
                alt={product.name}
                className="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-105"
              />

              {/* Badges */}
              <div className="absolute top-4 left-4 flex flex-col gap-2 pointer-events-none">
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-surface/95 text-tertiary font-label-md text-xs rounded-full backdrop-blur-md shadow-sm border border-border-bamboo/40">
                  <span className="w-2 h-2 rounded-full bg-accent-lotus-jade" />
                  100% Trầm Thật Tự Nhiên
                </span>
                {product.brand && (
                  <span className="inline-flex items-center px-3 py-1 bg-bg-agarwood-dark/85 text-secondary-fixed font-caption-meta text-xs rounded-full backdrop-blur-md tracking-wider">
                    {product.brand.toUpperCase()}
                  </span>
                )}
              </div>

              {/* Wishlist Button */}
              <button
                onClick={() => toggleWishlist(product.id)}
                aria-label="Lưu yêu thích"
                className={`absolute top-4 right-4 w-10 h-10 rounded-full bg-surface/90 backdrop-blur-md flex items-center justify-center transition-all shadow-md ${
                  isFav ? 'text-status-cinnabar scale-110' : 'text-text-wood-muted hover:text-status-cinnabar'
                }`}
              >
                <span
                  className="material-symbols-outlined text-[22px]"
                  style={{ fontVariationSettings: isFav ? "'FILL' 1" : "'FILL' 0" }}
                >
                  favorite
                </span>
              </button>
            </div>

            {/* Thumbnail Strip */}
            {images.length > 1 && (
              <div className="grid grid-cols-5 gap-3">
                {images.map((img, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => setSelectedImage(img.imageUrl)}
                    className={`aspect-square rounded-xl overflow-hidden bg-bg-warm-sand border transition-all ${
                      selectedImage === img.imageUrl
                        ? 'border-2 border-secondary ring-2 ring-secondary/30 scale-102'
                        : 'border-border-bamboo/60 opacity-70 hover:opacity-100'
                    }`}
                  >
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={img.imageUrl} alt="" className="w-full h-full object-cover" />
                  </button>
                ))}
              </div>
            )}

            {/* Trust Origin Certificate Note */}
            <div className="p-4 rounded-xl bg-bg-warm-sand/80 border border-border-bamboo flex items-center justify-between gap-3 text-xs text-text-wood-muted">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-secondary text-[20px]">verified_user</span>
                <span>
                  Mã chứng thư vùng nguyên liệu:{' '}
                  <strong className="text-primary font-mono">{product.skuPrefix || 'MH-TRAM-2026'}</strong>
                </span>
              </div>
              <span className="text-secondary font-semibold">Tuyển chọn Tiên Phước</span>
            </div>
          </div>

          {/* Right Column: Details, Variant Selector, Pricing, Add To Cart */}
          <div className="lg:col-span-6 flex flex-col gap-5 bg-bg-warm-sand/50 p-6 lg:p-8 rounded-3xl border border-border-bamboo/80 shadow-sm">
            {/* SKU & Brand */}
            <div className="flex items-center justify-between flex-wrap gap-2">
              <span className="px-3 py-1 bg-surface rounded-full text-xs font-semibold text-primary border border-border-bamboo/50">
                {product.brand || 'MỘC HƯƠNG'}
              </span>
              <span className="text-xs font-mono text-text-wood-muted">
                SKU: {selectedVariant?.sku || product.skuPrefix}
              </span>
            </div>

            {/* Product Title */}
            <div>
              <h1 className="font-serif text-2xl sm:text-3xl lg:text-4xl text-primary font-bold leading-tight">
                {product.name}
              </h1>
              <p className="font-serif italic text-text-wood-muted text-sm mt-1">
                “Hương ngưng tụ từ ngàn dặm núi rừng, dưỡng tâm tĩnh lạc giữa đời thường”
              </p>
            </div>

            {/* Social Proof Row */}
            <div className="flex items-center flex-wrap gap-x-4 gap-y-2 py-2.5 text-xs text-text-wood-muted bg-surface px-4 rounded-xl border border-border-bamboo/60">
              <div className="flex items-center gap-1">
                <span className="font-bold text-primary text-sm">
                  {product.averageRating ? product.averageRating.toFixed(1) : '5.0'}
                </span>
                <div className="flex text-secondary text-[16px]">
                  <span className="material-symbols-outlined text-[16px] fill-current">star</span>
                  <span className="material-symbols-outlined text-[16px] fill-current">star</span>
                  <span className="material-symbols-outlined text-[16px] fill-current">star</span>
                  <span className="material-symbols-outlined text-[16px] fill-current">star</span>
                  <span className="material-symbols-outlined text-[16px] fill-current">star</span>
                </div>
                <span className="underline ml-1">({reviews.length || product.reviewCount || 12} đánh giá)</span>
              </div>
              <span className="text-outline-variant font-mono">•</span>
              <div>
                Đã chuyển giao: <strong className="text-primary font-semibold">{product.totalSold || 350} phẩm</strong>
              </div>
            </div>

            {/* Price Block */}
            <div className="p-4 rounded-2xl bg-surface border border-border-bamboo flex items-baseline justify-between gap-3">
              <div className="flex items-baseline gap-3">
                <span className="font-serif text-3xl font-bold text-primary">
                  {formatCurrency(currentPrice)}
                </span>
              </div>
              <span className="px-3 py-1 bg-accent-lotus-jade/15 text-accent-lotus-jade rounded-full text-xs font-semibold">
                Miễn phí giao hàng đơn từ 500k
              </span>
            </div>

            {/* Description */}
            <p className="font-body-base text-sm text-text-ink-black leading-relaxed">
              {product.description}
            </p>

            {/* Variant Pills Selector */}
            {product.variants && product.variants.length > 0 && (
              <div className="flex flex-col gap-2 pt-2">
                <div className="flex items-center justify-between">
                  <label className="font-label-lg text-xs font-bold text-primary uppercase tracking-wider">
                    Chọn Phiên Bản & Biến Thể:
                  </label>
                  <span className="text-xs text-text-wood-muted">
                    {availableStock > 0 ? (
                      <span className="text-accent-lotus-jade font-semibold flex items-center gap-1">
                        <span className="w-2 h-2 rounded-full bg-accent-lotus-jade" />
                        Còn {availableStock} trong kho
                      </span>
                    ) : (
                      <span className="text-status-cinnabar font-semibold">Tạm hết hàng</span>
                    )}
                  </span>
                </div>

                <div className="flex flex-col gap-2">
                  {product.variants.map((v) => {
                    const isSelected = selectedVariant?.id === v.id;
                    return (
                      <button
                        key={v.id}
                        type="button"
                        onClick={() => handleVariantChange(v)}
                        className={`p-3.5 rounded-xl border text-left transition-all flex items-center justify-between ${
                          isSelected
                            ? 'bg-primary text-secondary-fixed border-primary shadow-md'
                            : 'bg-surface text-text-ink-black border-border-bamboo hover:border-primary'
                        }`}
                      >
                        <div className="flex items-center gap-2">
                          <span
                            className={`w-3.5 h-3.5 rounded-full border flex items-center justify-center ${
                              isSelected ? 'border-secondary-fixed bg-secondary-fixed' : 'border-border-bamboo'
                            }`}
                          >
                            {isSelected && <span className="w-1.5 h-1.5 rounded-full bg-primary" />}
                          </span>
                          <span className="font-label-md text-xs font-semibold">{v.variantName}</span>
                          <span className={`text-[11px] font-mono ${isSelected ? 'opacity-80' : 'text-text-wood-muted'}`}>
                            ({v.sku})
                          </span>
                        </div>
                        <span className="font-mono text-sm font-bold">
                          {formatCurrency(v.price)}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Quantity Stepper & CTAs */}
            <div className="flex flex-col gap-3 pt-3 border-t border-border-bamboo">
              <div className="flex items-center gap-4">
                <span className="text-xs font-bold uppercase tracking-wider text-primary">Số Lượng:</span>
                <div className="flex items-center border border-border-bamboo rounded-xl bg-surface overflow-hidden">
                  <button
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                    className="w-9 h-9 flex items-center justify-center text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
                  >
                    <span className="material-symbols-outlined text-[16px]">remove</span>
                  </button>
                  <span className="w-12 text-center font-mono text-sm font-bold text-text-ink-black">
                    {quantity}
                  </span>
                  <button
                    onClick={() => setQuantity((q) => Math.min(availableStock || 99, q + 1))}
                    className="w-9 h-9 flex items-center justify-center text-text-wood-muted hover:text-primary hover:bg-surface-container transition-colors"
                  >
                    <span className="material-symbols-outlined text-[16px]">add</span>
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                <button
                  onClick={handleAddToCart}
                  disabled={availableStock <= 0}
                  className="py-3.5 px-4 rounded-xl bg-surface border-2 border-primary text-primary font-label-lg font-bold hover:bg-surface-container transition-all flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
                >
                  <span className="material-symbols-outlined text-[20px]">add_shopping_cart</span>
                  <span>Thêm Vào Giỏ</span>
                </button>

                <button
                  onClick={handleBuyNow}
                  disabled={availableStock <= 0}
                  className="py-3.5 px-4 rounded-xl bg-primary text-secondary-fixed font-label-lg font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
                >
                  <span>Mua Ngay 1-Chạm</span>
                  <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                </button>
              </div>
            </div>
          </div>
        </section>

        {/* 3. Detailed Tabs (Thông tin chi tiết, Nguồn gốc, Đánh giá) */}
        <section className="mt-8 bg-surface rounded-3xl border border-border-bamboo p-6 lg:p-10 shadow-sm">
          {/* Tab Headers */}
          <div className="flex border-b border-border-bamboo/60 gap-4 sm:gap-8 pb-3">
            {[
              { id: 'details', label: 'Chi Tiết & Quy Chuẩn' },
              { id: 'origin', label: 'Xuất Xứ & Bảo Quản' },
              { id: 'reviews', label: `Đánh Giá Khách Hàng (${reviews.length})` },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`font-serif text-base sm:text-lg font-bold pb-2 transition-all relative ${
                  activeTab === tab.id
                    ? 'text-primary'
                    : 'text-text-wood-muted hover:text-primary'
                }`}
              >
                {tab.label}
                {activeTab === tab.id && (
                  <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-primary rounded-full" />
                )}
              </button>
            ))}
          </div>

          {/* Tab 1: Details */}
          {activeTab === 'details' && (
            <div className="py-6 space-y-4 text-sm text-text-ink-black leading-relaxed">
              <h3 className="font-serif text-xl font-bold text-primary">Thông Tin Kỹ Thuật & Đặc Tính</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-bg-warm-sand/50 p-6 rounded-2xl border border-border-bamboo">
                <div>
                  <span className="font-bold text-text-wood-muted text-xs uppercase">Thương hiệu:</span>
                  <p className="font-semibold text-primary">{product.brand || 'Mộc Hương'}</p>
                </div>
                <div>
                  <span className="font-bold text-text-wood-muted text-xs uppercase">Vùng tạo trầm:</span>
                  <p className="font-semibold text-primary">Tiên Phước, Quảng Nam</p>
                </div>
                <div>
                  <span className="font-bold text-text-wood-muted text-xs uppercase">Tiêu chuẩn kiểm nghiệm:</span>
                  <p className="font-semibold text-primary">100% Không hương liệu tổng hợp, Không hóa chất độc hại</p>
                </div>
                <div>
                  <span className="font-bold text-text-wood-muted text-xs uppercase">Mã tiền tố SKU:</span>
                  <p className="font-mono text-primary font-semibold">{product.skuPrefix}</p>
                </div>
              </div>
              <p className="mt-4">{product.description}</p>
            </div>
          )}

          {/* Tab 2: Origin */}
          {activeTab === 'origin' && (
            <div className="py-6 space-y-4 text-sm text-text-ink-black leading-relaxed">
              <h3 className="font-serif text-xl font-bold text-primary">Nguồn Gốc & Nghệ Thuật Thưởng Hương</h3>
              <p>
                Trầm hương Tiên Phước vốn nổi danh khắp bốn phương bởi chất dầu trầm sẫm màu, lượng tinh dầu ngưng tụ
                đậm đặc và mùi hương thơm ngọt hậu vị hoa quả chín mộc mạc.
              </p>
              <div className="bg-bg-warm-sand/50 p-6 rounded-2xl border border-border-bamboo space-y-2">
                <h4 className="font-bold text-primary text-base">Hướng Dẫn Bảo Quản & Sử Dụng:</h4>
                <ul className="list-disc pl-5 space-y-1 text-text-wood-muted">
                  <li>Bảo quản nơi khô ráo, thoáng gió, tránh ánh nắng trực tiếp hoặc nơi ẩm mốc.</li>
                  <li>Đối với vòng gỗ: Hạn chế ngâm trong nước lâu hoặc tiếp xúc với hóa chất tẩy rửa mạnh.</li>
                  <li>Đối with nụ và nhang: Đậy kín nắp hộp sau khi sử dụng để lưu giữ trọn vẹn mùi hương tự nhiên.</li>
                </ul>
              </div>
            </div>
          )}

          {/* Tab 3: Customer Reviews */}
          {activeTab === 'reviews' && (
            <div className="py-6 space-y-8">
              {/* Write Review Form */}
              <div className="bg-bg-warm-sand/50 p-6 rounded-2xl border border-border-bamboo">
                <h3 className="font-serif text-lg font-bold text-primary mb-3">Gửi Cảm Nhận Của Bạn Về Sản Phẩm</h3>
                <form onSubmit={handleReviewSubmit} className="space-y-4">
                  <div>
                    <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                      Mức Độ Hài Lòng (1-5 Sao):
                    </label>
                    <div className="flex items-center gap-2">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onClick={() => setRatingInput(star)}
                          className="text-secondary hover:scale-110 transition-transform"
                        >
                          <span
                            className="material-symbols-outlined text-[28px]"
                            style={{ fontVariationSettings: star <= ratingInput ? "'FILL' 1" : "'FILL' 0" }}
                          >
                            star
                          </span>
                        </button>
                      ))}
                      <span className="font-bold text-primary text-sm ml-2">{ratingInput} / 5 Sao</span>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                      Nhận Xét Của Bạn:
                    </label>
                    <textarea
                      rows={3}
                      value={commentInput}
                      onChange={(e) => setCommentInput(e.target.value)}
                      placeholder="Chia sẻ cảm nhận về mùi hương, độ tinh tế của sản phẩm..."
                      className="w-full p-3 bg-surface rounded-xl border border-border-bamboo text-sm text-text-ink-black focus:outline-none focus:border-primary"
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={isSubmittingReview}
                    className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm disabled:opacity-50"
                  >
                    {isSubmittingReview ? 'Đang gửi...' : 'Gửi Đánh Giá'}
                  </button>
                </form>
              </div>

              {/* Reviews List */}
              <div className="space-y-4">
                <h3 className="font-serif text-lg font-bold text-primary">Các Đánh Giá Đã Gửi</h3>
                {reviews.length === 0 ? (
                  <p className="text-xs text-text-wood-muted italic">
                    Chưa có đánh giá nào cho sản phẩm này. Hãy là người đầu tiên chia sẻ cảm nhận!
                  </p>
                ) : (
                  reviews.map((rev) => (
                    <div
                      key={rev.id}
                      className="p-5 rounded-2xl bg-bg-warm-sand/30 border border-border-bamboo/60 space-y-2"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <div className="w-8 h-8 rounded-full bg-primary text-secondary-fixed flex items-center justify-center font-bold text-xs">
                            {rev.userName?.charAt(0) || 'K'}
                          </div>
                          <div>
                            <span className="font-semibold text-text-ink-black text-sm block">
                              {rev.userName || 'Khách Hàng Mộc Hương'}
                            </span>
                            <span className="text-[11px] text-text-wood-muted">{formatDate(rev.createdAt)}</span>
                          </div>
                        </div>

                        <div className="flex text-secondary">
                          {[...Array(5)].map((_, i) => (
                            <span
                              key={i}
                              className="material-symbols-outlined text-[16px]"
                              style={{ fontVariationSettings: i < rev.rating ? "'FILL' 1" : "'FILL' 0" }}
                            >
                              star
                            </span>
                          ))}
                        </div>
                      </div>

                      <p className="text-sm text-text-ink-black leading-relaxed">
                        {rev.comment || (rev as any).content}
                      </p>

                      {/* Staff/Admin Reply */}
                      {(rev.replyMessage || (rev as any).adminReply) && (
                        <div className="mt-3 pl-4 border-l-2 border-secondary bg-surface p-3 rounded-r-xl">
                          <span className="text-xs font-bold text-secondary uppercase block">
                            Phản hồi từ Mộc Hương:
                          </span>
                          <p className="text-xs text-text-wood-muted mt-0.5">
                            {rev.replyMessage || (rev as any).adminReply}
                          </p>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

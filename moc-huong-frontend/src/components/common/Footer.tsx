import React from 'react';
import Link from 'next/link';

export default function Footer() {
  return (
    <footer className="w-full bg-bg-agarwood-dark text-surface-dim pt-space-xl pb-space-lg border-t border-border-bamboo/20">
      <div className="max-w-[1360px] mx-auto px-margin-mobile lg:px-margin">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-space-lg pb-space-xl">
          {/* Col 1: Brand Info */}
          <div className="lg:col-span-4 flex flex-col gap-space-sm">
            <div className="flex items-center gap-space-sm">
              <div className="w-9 h-9 rounded-full bg-secondary-fixed/20 flex items-center justify-center text-secondary-fixed">
                <span className="material-symbols-outlined text-[22px]">spa</span>
              </div>
              <span className="font-headline-sm text-headline-sm text-surface-bright tracking-wide font-serif">
                MỘC HƯƠNG
              </span>
            </div>
            <p className="font-body-sm text-body-sm text-surface-variant max-w-sm mt-space-xs leading-relaxed">
              Mộc Hương mang tinh hoa trầm Việt thuần khiết và phong cách thiền định tĩnh lặng vào từng không gian sống.
            </p>
            <div className="flex flex-col gap-1.5 font-caption-meta text-caption-meta text-surface-variant/80 mt-space-xs">
              <div className="flex items-start gap-2">
                <span className="material-symbols-outlined text-[16px] text-secondary-fixed-dim">location_on</span>
                <span>128 Nguyễn Trãi, P. Bến Thành, Quận 1, TP. Hồ Chí Minh</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-[16px] text-secondary-fixed-dim">call</span>
                <span>Hotline: 1900 8888 (8h00 - 21h00)</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-[16px] text-secondary-fixed-dim">mail</span>
                <span>contact@mochuong.vn</span>
              </div>
            </div>
          </div>

          {/* Col 2: Categories */}
          <div className="lg:col-span-2 flex flex-col gap-space-xs">
            <h4 className="font-label-lg text-label-lg text-secondary-fixed tracking-wide uppercase mb-space-xs font-semibold">
              Danh Mục
            </h4>
            <Link href="/shop?categoryId=1" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Nhang Trầm Tự Nhiên
            </Link>
            <Link href="/shop?categoryId=1" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Nụ Trầm Thác Khói
            </Link>
            <Link href="/shop?categoryId=2" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Tinh Dầu Nguyên Chất
            </Link>
            <Link href="/shop?categoryId=3" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Vòng Tay Trầm Hương
            </Link>
            <Link href="/shop?categoryId=4" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Dụng Cụ Thưởng Trầm
            </Link>
          </div>

          {/* Col 3: Customer Care */}
          <div className="lg:col-span-3 flex flex-col gap-space-xs">
            <h4 className="font-label-lg text-label-lg text-secondary-fixed tracking-wide uppercase mb-space-xs font-semibold">
              Chăm Sóc Khách Hàng
            </h4>
            <Link href="/about" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Chính Sách Đổi Trả 7 Ngày
            </Link>
            <Link href="/about" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Cam Kết Trầm Thật 100%
            </Link>
            <Link href="/about" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Hướng Dẫn Bảo Quản Vòng Gỗ
            </Link>
            <Link href="/about" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Phương Thức Vận Chuyển
            </Link>
            <Link href="/about" className="font-body-sm text-body-sm text-surface-variant hover:text-surface-bright transition-colors">
              Câu Hỏi Thường Gặp (FAQ)
            </Link>
          </div>

          {/* Col 4: Newsletter & Payments */}
          <div className="lg:col-span-3 flex flex-col gap-space-sm">
            <h4 className="font-label-lg text-label-lg text-secondary-fixed tracking-wide uppercase font-semibold">
              Bản Tin Thiền Định
            </h4>
            <p className="font-body-sm text-body-sm text-surface-variant leading-relaxed">
              Nhận bài viết an yên, câu chuyện Trầm đạo và ưu đãi độc quyền từ Mộc Hương.
            </p>
            <div className="flex items-center bg-surface-container-highest/10 rounded-lg p-1 border border-border-bamboo/20">
              <input
                className="bg-transparent font-body-sm text-body-sm text-surface-bright px-space-xs placeholder:text-surface-dim/60 focus:outline-none flex-1"
                placeholder="Email của bạn..."
                type="email"
              />
              <button className="bg-secondary-fixed text-bg-agarwood-dark font-label-md text-label-md px-space-sm py-1.5 rounded hover:bg-secondary-container transition-colors font-semibold">
                Đăng Ký
              </button>
            </div>
            <div className="flex flex-col gap-1 mt-space-xs">
              <span className="font-caption-meta text-caption-meta text-surface-variant/80 uppercase tracking-wider text-[11px]">
                Phương Thức Thanh Toán
              </span>
              <div className="flex items-center gap-space-xs flex-wrap">
                <span className="bg-surface-container-lowest/10 text-surface-bright font-caption-meta px-2 py-0.5 rounded font-mono text-[11px]">
                  VNPAY
                </span>
                <span className="bg-surface-container-lowest/10 text-surface-bright font-caption-meta px-2 py-0.5 rounded font-mono text-[11px]">
                  MoMo
                </span>
                <span className="bg-surface-container-lowest/10 text-surface-bright font-caption-meta px-2 py-0.5 rounded font-mono text-[11px]">
                  VISA/MC
                </span>
                <span className="bg-surface-container-lowest/10 text-surface-bright font-caption-meta px-2 py-0.5 rounded font-mono text-[11px]">
                  COD
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="pt-space-md mt-space-md border-t border-border-bamboo/15 text-center font-caption-meta text-caption-meta text-surface-dim/60 flex flex-col sm:flex-row items-center justify-between gap-space-sm text-[12px]">
          <span>© 2026 Mộc Hương Aromatics (aromatics-platform). Bản quyền thuộc về Mộc Hương.</span>
          <div className="flex items-center gap-space-md">
            <Link href="/about" className="hover:text-surface-bright transition-colors">Chính Sách Bảo Mật</Link>
            <Link href="/about" className="hover:text-surface-bright transition-colors">Điều Khoản Dịch Vụ</Link>
            <Link href="/about" className="hover:text-surface-bright transition-colors">Chứng Nhận Chất Lượng</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}

'use client';

import React, { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';

function VNPayReturnContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const responseCode = searchParams.get('vnp_ResponseCode');
  const orderInfo = searchParams.get('vnp_OrderInfo') || '';
  const txnRef = searchParams.get('vnp_TxnRef') || '';

  const isSuccess = responseCode === '00';
  const [countdown, setCountdown] = useState(5);

  // Extract order code if possible
  const orderCodeMatch = orderInfo.match(/MH-[A-Za-z0-9_-]+/);
  const orderCode = orderCodeMatch ? orderCodeMatch[0] : txnRef;

  useEffect(() => {
    if (isSuccess && orderCode) {
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            router.push(`/orders/success/${orderCode}`);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [isSuccess, orderCode, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 py-12">
      <div className="max-w-md w-full bg-surface rounded-3xl border border-border-bamboo p-8 text-center space-y-6 shadow-lg">
        {isSuccess ? (
          <>
            <div className="w-20 h-20 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade flex items-center justify-center mx-auto border border-accent-lotus-jade/30">
              <span className="material-symbols-outlined text-[44px]">verified</span>
            </div>
            <div>
              <h1 className="font-serif text-2xl font-bold text-primary">Thanh Toán VNPay Thành Công!</h1>
              <p className="font-body-base text-text-wood-muted text-sm mt-2">
                Giao dịch của bạn đã được xác thực an toàn qua cổng VNPay. Tự động chuyển đến đơn hàng trong{' '}
                <strong className="text-primary font-mono">{countdown}s</strong>...
              </p>
            </div>
            <Link
              href={orderCode ? `/orders/success/${orderCode}` : '/account/orders'}
              className="inline-block w-full py-3 rounded-xl bg-primary text-secondary-fixed font-label-md font-bold hover:bg-primary-container transition-colors shadow-sm"
            >
              Xem Đơn Hàng Ngay
            </Link>
          </>
        ) : (
          <>
            <div className="w-20 h-20 rounded-full bg-status-cinnabar/15 text-status-cinnabar flex items-center justify-center mx-auto border border-status-cinnabar/30">
              <span className="material-symbols-outlined text-[44px]">error</span>
            </div>
            <div>
              <h1 className="font-serif text-2xl font-bold text-primary">Giao Dịch Chưa Thành Công</h1>
              <p className="font-body-base text-text-wood-muted text-sm mt-2">
                Thanh toán qua VNPay đã bị hủy hoặc gặp gián đoạn (Mã lỗi:{' '}
                <span className="font-mono">{responseCode || 'N/A'}</span>). Đơn hàng của quý khách vẫn được lưu giữ ở
                trạng thái Chờ thanh toán.
              </p>
            </div>
            <div className="flex flex-col gap-2">
              <Link
                href="/account/orders"
                className="w-full py-3 rounded-xl bg-primary text-secondary-fixed font-label-md font-bold hover:bg-primary-container transition-colors shadow-sm"
              >
                Xem Lịch Sử Đơn Hàng
              </Link>
              <Link
                href="/shop"
                className="w-full py-2.5 rounded-xl bg-surface-container text-text-wood-muted font-label-md hover:text-primary transition-colors text-xs"
              >
                Quay lại Cửa Hàng
              </Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default function VNPayReturnPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-background">
          <span className="material-symbols-outlined text-[36px] text-primary animate-spin">spa</span>
        </div>
      }
    >
      <VNPayReturnContent />
    </Suspense>
  );
}

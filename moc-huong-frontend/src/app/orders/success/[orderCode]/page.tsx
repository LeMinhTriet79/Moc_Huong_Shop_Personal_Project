'use client';

import React, { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { orderService } from '@/services/orderService';
import { OrderResponse } from '@/types';
import { formatCurrency, formatDate, ORDER_STATUS_MAP } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function OrderSuccessPage() {
  const params = useParams();
  const orderCode = params?.orderCode as string;

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isDownloading, setIsDownloading] = useState<boolean>(false);

  useEffect(() => {
    async function loadOrder() {
      if (!orderCode) return;
      try {
        const data = await orderService.getOrderByCode(orderCode);
        setOrder(data);
      } catch (e) {
        console.error('Failed to load order:', e);
      } finally {
        setIsLoading(false);
      }
    }
    loadOrder();
  }, [orderCode]);

  const handleDownloadPdf = async () => {
    if (!order) return;
    setIsDownloading(true);
    try {
      await orderService.downloadInvoicePdf(order.id, order.orderCode);
      toast.success('Đã tải hóa đơn PDF thành công!');
    } catch (e) {
      console.error('Download invoice failed:', e);
      toast.error('Không thể tải hóa đơn PDF lúc này');
    } finally {
      setIsDownloading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <span className="material-symbols-outlined text-[40px] text-primary animate-spin">spa</span>
          <p className="font-serif text-primary text-base">Đang tải chi tiết đơn hàng...</p>
        </div>
      </div>
    );
  }

  const statusConfig = order?.status
    ? ORDER_STATUS_MAP[order.status]
    : { label: 'Đang xử lý', bg: 'bg-secondary-fixed/40', text: 'text-primary', icon: 'schedule' };

  return (
    <div className="w-full bg-background min-h-screen py-12">
      <div className="max-w-[800px] mx-auto px-margin-mobile lg:px-margin">
        <div className="bg-surface rounded-3xl border border-border-bamboo p-8 sm:p-12 shadow-md text-center space-y-6">
          {/* Success Lotus Icon */}
          <div className="w-20 h-20 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade flex items-center justify-center mx-auto shadow-inner border border-accent-lotus-jade/30">
            <span className="material-symbols-outlined text-[44px]">check_circle</span>
          </div>

          <div>
            <span className="font-caption-meta text-xs uppercase tracking-widest text-secondary font-semibold">
              Kính Chúc Quý Khách An Lành
            </span>
            <h1 className="font-serif text-3xl sm:text-4xl text-primary font-bold mt-1">
              Đặt Hàng Thành Công!
            </h1>
            <p className="font-body-base text-text-wood-muted text-sm max-w-md mx-auto mt-2">
              Mộc Hương đã tiếp nhận đơn hàng của quý khách. Nghệ nhân sẽ cẩn trọng chuẩn bị và đóng gói từng nén hương
              thủ công gửi tới bạn.
            </p>
          </div>

          {/* Order Code & Status Badge */}
          <div className="p-4 rounded-2xl bg-bg-warm-sand border border-border-bamboo flex flex-wrap items-center justify-between gap-3 text-left">
            <div>
              <span className="text-xs text-text-wood-muted block uppercase">Mã Đơn Hàng</span>
              <span className="font-mono text-lg font-bold text-primary">{orderCode}</span>
              {order?.createdAt && (
                <span className="text-[11px] text-text-wood-muted block mt-0.5">
                  Ngày đặt: {formatDate(order.createdAt)}
                </span>
              )}
            </div>
            <div className="flex items-center gap-2">
              <span className={`px-3 py-1.5 rounded-full text-xs font-semibold flex items-center gap-1.5 ${statusConfig.bg} ${statusConfig.text}`}>
                <span className="material-symbols-outlined text-[16px]">{statusConfig.icon}</span>
                {statusConfig.label}
              </span>
            </div>
          </div>

          {/* Order Details Breakdown */}
          {order && (
            <div className="text-left space-y-4 pt-4 border-t border-border-bamboo">
              <h3 className="font-serif text-lg font-bold text-primary">Danh Sách Phẩm Hương Đã Đặt</h3>
              <div className="divide-y divide-border-bamboo/50 bg-bg-warm-sand/40 rounded-2xl p-4 border border-border-bamboo">
                {order.items?.map((item) => (
                  <div key={item.id} className="py-3 first:pt-0 last:pb-0 flex items-center justify-between gap-3">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-lg bg-surface border border-border-bamboo overflow-hidden shrink-0">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img src={item.productImageUrl} alt={item.productName} className="w-full h-full object-cover" />
                      </div>
                      <div>
                        <p className="text-xs font-bold text-text-ink-black">{item.productName}</p>
                        <p className="text-[11px] text-text-wood-muted">{item.variantName} × {item.quantity}</p>
                      </div>
                    </div>
                    <span className="font-mono text-xs font-bold text-primary">
                      {formatCurrency(item.totalPrice)}
                    </span>
                  </div>
                ))}

                <div className="pt-3 mt-3 border-t border-border-bamboo flex items-baseline justify-between text-primary">
                  <span className="font-serif font-bold text-sm">Tổng Thanh Toán:</span>
                  <span className="font-mono text-xl font-bold">{formatCurrency(order.totalAmount)}</span>
                </div>
              </div>

              {/* Delivery Address Details */}
              {order.shippingAddress && (
                <div className="bg-bg-warm-sand/30 p-4 rounded-2xl border border-border-bamboo text-xs space-y-1">
                  <span className="font-bold text-primary uppercase text-[11px] block">Địa Chỉ Giao Hàng:</span>
                  <p className="font-semibold text-text-ink-black">
                    {order.shippingAddress.recipientName} • {order.shippingAddress.phoneNumber}
                  </p>
                  <p className="text-text-wood-muted">
                    {order.shippingAddress.streetAddress}, {order.shippingAddress.ward},{' '}
                    {order.shippingAddress.district}, {order.shippingAddress.province}
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Action Buttons */}
          <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-3">
            {order && (
              <button
                onClick={handleDownloadPdf}
                disabled={isDownloading}
                className="w-full sm:w-auto px-6 py-3 rounded-xl bg-surface border-2 border-primary text-primary font-label-md font-bold hover:bg-surface-container transition-all flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-[18px]">download</span>
                <span>{isDownloading ? 'Đang xuất PDF...' : 'Tải Hóa Đơn PDF'}</span>
              </button>
            )}

            <Link
              href="/account/orders"
              className="w-full sm:w-auto px-6 py-3 rounded-xl bg-primary text-secondary-fixed font-label-md font-bold hover:bg-primary-container transition-all flex items-center justify-center gap-2 shadow-md"
            >
              <span>Xem Đơn Trong Hồ Sơ</span>
              <span className="material-symbols-outlined text-[18px]">receipt_long</span>
            </Link>

            <Link
              href="/shop"
              className="w-full sm:w-auto px-6 py-3 rounded-xl bg-bg-warm-sand text-primary font-label-md font-semibold hover:bg-surface-container transition-colors"
            >
              Tiếp Tục Dạo Cửa Hàng
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

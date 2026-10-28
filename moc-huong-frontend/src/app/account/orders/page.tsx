'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { orderService } from '@/services/orderService';
import { catalogService } from '@/services/catalogService';
import { OrderResponse } from '@/types';
import { formatCurrency, formatDate, getOrderStatusInfo } from '@/lib/utils';
import toast from 'react-hot-toast';

const STATUS_TABS: { label: string; value: string }[] = [
  { label: 'Tất Cả', value: 'ALL' },
  { label: 'Chờ Thanh Toán', value: 'PENDING' },
  { label: 'Đã Xác Nhận', value: 'CONFIRMED' },
  { label: 'Đang Chuẩn Bị', value: 'PROCESSING' },
  { label: 'Đang Giao Hàng', value: 'SHIPPED' },
  { label: 'Đã Giao Thành Công', value: 'DELIVERED' },
  { label: 'Đã Hủy', value: 'CANCELLED' },
];

export default function OrderHistoryPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [activeTab, setActiveTab] = useState<string>('ALL');
  const [searchCode, setSearchCode] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Cancel Modal State
  const [cancelModalOrder, setCancelModalOrder] = useState<OrderResponse | null>(null);
  const [cancelReason, setCancelReason] = useState<string>('Thay đổi nhu cầu');
  const [isCancelling, setIsCancelling] = useState<boolean>(false);

  // Return Modal State
  const [returnModalOrder, setReturnModalOrder] = useState<OrderResponse | null>(null);
  const [returnReason, setReturnReason] = useState<string>('');
  const [isReturning, setIsReturning] = useState<boolean>(false);

  // Review Modal State
  const [reviewItem, setReviewItem] = useState<{
    orderId: number;
    productId: number;
    productName: string;
  } | null>(null);
  const [reviewRating, setReviewRating] = useState<number>(5);
  const [reviewComment, setReviewComment] = useState<string>('');
  const [isSubmittingReview, setIsSubmittingReview] = useState<boolean>(false);

  const fetchOrders = async () => {
    setIsLoading(true);
    try {
      const pageData = await orderService.getMyOrders(activeTab, 0, 20);
      setOrders(pageData.content || []);
    } catch (e) {
      console.error('Failed to load orders:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [activeTab]);

  const handleDownloadPdf = async (order: OrderResponse) => {
    try {
      await orderService.downloadInvoicePdf(order.id, order.orderCode);
      toast.success('Đã tải hóa đơn PDF!');
    } catch (e) {
      toast.error('Không thể tải hóa đơn PDF');
    }
  };

  const handleCancelSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cancelModalOrder) return;
    setIsCancelling(true);
    try {
      await orderService.cancelOrder(cancelModalOrder.id, cancelReason);
      toast.success(`Đã hủy đơn hàng ${cancelModalOrder.orderCode}`);
      setCancelModalOrder(null);
      fetchOrders();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể hủy đơn hàng');
    } finally {
      setIsCancelling(false);
    }
  };

  const handleReturnSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!returnModalOrder || !returnReason.trim()) {
      toast.error('Vui lòng nêu lý do trả hàng');
      return;
    }
    setIsReturning(true);
    try {
      await orderService.requestReturn(returnModalOrder.id, returnReason.trim());
      toast.success(`Đã gửi yêu cầu hoàn trả cho đơn ${returnModalOrder.orderCode}`);
      setReturnModalOrder(null);
      fetchOrders();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể yêu cầu trả hàng');
    } finally {
      setIsReturning(false);
    }
  };

  const handleReviewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reviewItem || !reviewComment.trim()) {
      toast.error('Vui lòng nhập nhận xét');
      return;
    }
    setIsSubmittingReview(true);
    try {
      await catalogService.createReview({
        productId: reviewItem.productId,
        orderId: reviewItem.orderId,
        rating: reviewRating,
        comment: reviewComment.trim(),
      });
      toast.success('Cảm ơn bạn đã đánh giá sản phẩm!', { icon: '🌸' });
      setReviewItem(null);
      setReviewComment('');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể gửi đánh giá');
    } finally {
      setIsSubmittingReview(false);
    }
  };

  const filteredOrders = searchCode.trim()
    ? orders.filter((o) => o.orderCode.toLowerCase().includes(searchCode.trim().toLowerCase()))
    : orders;

  return (
    <div className="space-y-6">
      {/* Top Strip */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="font-serif text-2xl font-bold text-primary">Lịch Sử Đơn Hàng & Vận Chuyển</h2>
            <p className="text-xs text-text-wood-muted mt-0.5">
              Theo dõi chu toàn từng chặng đường mang trầm hương thanh tịnh đến không gian sống của bạn
            </p>
          </div>

          <div className="relative">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
              search
            </span>
            <input
              type="text"
              value={searchCode}
              onChange={(e) => setSearchCode(e.target.value)}
              placeholder="Tìm theo mã đơn (MH-...)"
              className="pl-9 pr-3 py-2 bg-surface-container-low rounded-xl border border-border-bamboo font-mono text-xs text-text-ink-black focus:outline-none focus:border-primary w-full sm:w-60"
            />
          </div>
        </div>

        {/* Status Tabs */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 pt-2 scrollbar-none">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              onClick={() => setActiveTab(tab.value)}
              className={`px-4 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                activeTab === tab.value
                  ? 'bg-primary text-secondary-fixed shadow-md'
                  : 'bg-surface-container-low text-text-wood-muted hover:bg-surface-container hover:text-text-ink-black'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Orders List */}
      {isLoading ? (
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="h-64 rounded-3xl bg-bg-warm-sand animate-pulse border border-border-bamboo" />
          ))}
        </div>
      ) : filteredOrders.length === 0 ? (
        <div className="bg-bg-warm-sand/50 rounded-3xl p-12 text-center border border-dashed border-border-bamboo flex flex-col items-center">
          <div className="w-16 h-16 rounded-full bg-surface flex items-center justify-center text-text-wood-muted mb-4 border border-border-bamboo">
            <span className="material-symbols-outlined text-[32px]">receipt_long</span>
          </div>
          <h3 className="font-serif text-xl font-bold text-primary mb-2">Chưa có đơn hàng nào trong mục này</h3>
          <p className="text-xs text-text-wood-muted max-w-sm mb-6">
            Khám phá các sản phẩm Trầm hương và vật phẩm thiền định tại cửa hàng.
          </p>
          <Link
            href="/shop"
            className="px-6 py-2.5 rounded-xl bg-primary text-secondary-fixed text-xs font-semibold hover:bg-primary-container transition-colors shadow-sm"
          >
            Dạo Xem Cửa Hàng
          </Link>
        </div>
      ) : (
        <div className="space-y-6">
          {filteredOrders.map((order) => {
            const statusConfig = getOrderStatusInfo(order.status);

            const canCancel = ['PENDING', 'PENDING_PAYMENT', 'CONFIRMED', 'PREPARING', 'PROCESSING'].includes(order.status);
            const canReturn = order.status === 'DELIVERED';

            return (
              <div
                key={order.id}
                className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden"
              >
                {/* Order Top Bar */}
                <div className="bg-bg-warm-sand/80 p-5 lg:p-6 border-b border-border-bamboo flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex flex-wrap items-center gap-3">
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-text-wood-muted uppercase">Mã:</span>
                      <span className="font-mono text-sm font-bold text-primary bg-surface px-2.5 py-1 rounded-lg border border-border-bamboo">
                        {order.orderCode}
                      </span>
                    </div>
                    <span className="text-border-bamboo hidden sm:inline">•</span>
                    <span className="text-xs text-text-wood-muted">{formatDate(order.createdAt)}</span>
                    <span className="text-border-bamboo hidden sm:inline">•</span>
                    <span className="text-xs font-mono font-medium text-text-wood-muted">
                      {order.paymentMethod} ({order.paymentStatus === 'PAID' ? 'Đã Thanh Toán' : 'Chưa Thanh Toán'})
                    </span>
                  </div>

                  <div className="flex items-center gap-2.5">
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-1.5 ${statusConfig.bg} ${statusConfig.text}`}>
                      <span className="material-symbols-outlined text-[15px]">{statusConfig.icon}</span>
                      <span>{statusConfig.label}</span>
                    </span>

                    <button
                      onClick={() => handleDownloadPdf(order)}
                      className="p-1.5 rounded-lg bg-surface hover:bg-surface-container text-text-wood-muted hover:text-primary transition-colors border border-border-bamboo shadow-sm"
                      title="Tải hóa đơn PDF"
                    >
                      <span className="material-symbols-outlined text-[18px]">download</span>
                    </button>
                  </div>
                </div>

                {/* Items List */}
                <div className="p-5 lg:p-6 divide-y divide-border-bamboo/40 space-y-4">
                  {order.items?.map((item) => (
                    <div key={item.id} className="pt-4 first:pt-0 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                      <div className="flex items-center gap-4">
                        <div className="w-16 h-16 rounded-xl overflow-hidden bg-bg-warm-sand border border-border-bamboo shrink-0">
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img src={item.productImageUrl} alt={item.productName} className="w-full h-full object-cover" />
                        </div>
                        <div>
                          <Link href={`/products/${item.productId}`} className="font-serif font-bold text-sm text-text-ink-black hover:text-primary transition-colors">
                            {item.productName}
                          </Link>
                          <p className="text-xs text-text-wood-muted mt-0.5">
                            {item.variantName} • SKU: <span className="font-mono">{item.sku}</span>
                          </p>
                          <span className="font-mono text-xs text-text-wood-muted">
                            {formatCurrency(item.unitPrice)} × {item.quantity}
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center justify-between sm:justify-end gap-4">
                        <span className="font-mono text-sm font-bold text-primary">
                          {formatCurrency(item.totalPrice)}
                        </span>

                        {order.status === 'DELIVERED' && (
                          <button
                            onClick={() =>
                              setReviewItem({
                                orderId: order.id,
                                productId: item.productId,
                                productName: item.productName,
                              })
                            }
                            className="px-3 py-1.5 rounded-xl border border-secondary text-secondary hover:bg-secondary-fixed/20 text-xs font-semibold transition-colors flex items-center gap-1"
                          >
                            <span className="material-symbols-outlined text-[15px]">rate_review</span>
                            <span>Đánh Giá</span>
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>

                {/* Shipping & Footer Actions */}
                <div className="p-5 lg:p-6 bg-surface-container-low/50 border-t border-border-bamboo flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-xs">
                  <div className="text-text-wood-muted max-w-md">
                    <span className="font-bold text-text-ink-black block">Giao tới:</span>
                    <span>
                      {order.shippingAddress?.recipientName} ({order.shippingAddress?.phoneNumber}) •{' '}
                      {order.shippingAddress?.streetAddress}, {order.shippingAddress?.ward},{' '}
                      {order.shippingAddress?.district}, {order.shippingAddress?.province}
                    </span>
                  </div>

                  <div className="flex flex-col sm:items-end gap-2">
                    <div className="flex items-baseline gap-2">
                      <span className="text-text-wood-muted">Tổng thanh toán:</span>
                      <span className="font-mono text-lg font-bold text-primary">{formatCurrency(order.totalAmount)}</span>
                    </div>

                    <div className="flex items-center gap-2">
                      {canCancel && (
                        <button
                          onClick={() => setCancelModalOrder(order)}
                          className="px-3.5 py-1.5 rounded-xl border border-status-cinnabar text-status-cinnabar hover:bg-status-cinnabar/10 text-xs font-semibold transition-colors"
                        >
                          Hủy Đơn Hàng
                        </button>
                      )}

                      {canReturn && (
                        <button
                          onClick={() => setReturnModalOrder(order)}
                          className="px-3.5 py-1.5 rounded-xl border border-outline text-text-wood-muted hover:bg-surface text-xs font-semibold transition-colors"
                        >
                          Yêu Cầu Hoàn Trả
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Cancel Order Modal */}
      {cancelModalOrder && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 shadow-2xl space-y-4">
            <h3 className="font-serif text-xl font-bold text-primary">Hủy Đơn Hàng {cancelModalOrder.orderCode}</h3>
            <p className="text-xs text-text-wood-muted">
              Quý khách có chắc chắn muốn hủy đơn hàng này? Số lượng tồn kho và mã giảm giá (nếu có) sẽ được tự động
              hoàn trả vào hệ thống.
            </p>
            <form onSubmit={handleCancelSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Lý Do Hủy Đơn</label>
                <select
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                >
                  <option value="Thay đổi nhu cầu">Thay đổi nhu cầu</option>
                  <option value="Muốn đổi sang sản phẩm khác">Muốn đổi sang sản phẩm khác</option>
                  <option value="Thời gian giao hàng không phù hợp">Thời gian giao hàng không phù hợp</option>
                  <option value="Nhập sai thông tin địa chỉ">Nhập sai thông tin địa chỉ</option>
                  <option value="Khác">Lý do khác</option>
                </select>
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setCancelModalOrder(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  disabled={isCancelling}
                  className="px-5 py-2.5 bg-status-cinnabar text-surface text-xs font-semibold rounded-xl hover:opacity-90 disabled:opacity-50"
                >
                  {isCancelling ? 'Đang hủy...' : 'Xác Nhận Hủy'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Return Request Modal */}
      {returnModalOrder && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 shadow-2xl space-y-4">
            <h3 className="font-serif text-xl font-bold text-primary">Yêu Cầu Hoàn Trả Đơn {returnModalOrder.orderCode}</h3>
            <p className="text-xs text-text-wood-muted">
              Mộc Hương cam kết chính sách đổi trả 7 ngày cho mọi lỗi sản phẩm hoặc không hài lòng về mùi hương.
            </p>
            <form onSubmit={handleReturnSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Lý Do Hoàn Trả *</label>
                <textarea
                  rows={3}
                  value={returnReason}
                  onChange={(e) => setReturnReason(e.target.value)}
                  placeholder="Mô tả chi tiết tình trạng vật phẩm..."
                  required
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setReturnModalOrder(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  disabled={isReturning}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-semibold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isReturning ? 'Đang gửi...' : 'Gửi Yêu Cầu'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Write Review Modal */}
      {reviewItem && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 shadow-2xl space-y-4">
            <h3 className="font-serif text-xl font-bold text-primary">Đánh Giá Sản Phẩm</h3>
            <p className="text-xs text-text-wood-muted line-clamp-1">{reviewItem.productName}</p>
            <form onSubmit={handleReviewSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Mức Độ Hài Lòng:</label>
                <div className="flex items-center gap-1.5">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setReviewRating(star)}
                      className="text-secondary hover:scale-110 transition-transform"
                    >
                      <span
                        className="material-symbols-outlined text-[28px]"
                        style={{ fontVariationSettings: star <= reviewRating ? "'FILL' 1" : "'FILL' 0" }}
                      >
                        star
                      </span>
                    </button>
                  ))}
                  <span className="font-bold text-primary text-xs ml-2">{reviewRating} / 5 Sao</span>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">Cảm Nhận Của Bạn *</label>
                <textarea
                  rows={3}
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="Chia sẻ trải nghiệm hương thơm và chất lượng đóng gói..."
                  required
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setReviewItem(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  disabled={isSubmittingReview}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-semibold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isSubmittingReview ? 'Đang gửi...' : 'Gửi Nhận Xét'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

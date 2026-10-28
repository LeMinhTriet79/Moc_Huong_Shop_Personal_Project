'use client';

import React, { useState, useEffect } from 'react';
import { orderService } from '@/services/orderService';
import { OrderResponse } from '@/types';
import { formatCurrency, formatDate, getOrderStatusInfo } from '@/lib/utils';
import toast from 'react-hot-toast';

const STATUS_FILTERS = [
  { label: 'Tất Cả', value: '' },
  { label: 'Chờ Thanh Toán', value: 'PENDING_PAYMENT' },
  { label: 'Đã Xác Nhận', value: 'CONFIRMED' },
  { label: 'Đang Chuẩn Bị', value: 'PREPARING' },
  { label: 'Đang Vận Chuyển', value: 'SHIPPING' },
  { label: 'Đã Giao Thành Công', value: 'DELIVERED' },
  { label: 'Hoàn Tất', value: 'COMPLETED' },
  { label: 'Đã Hủy', value: 'CANCELLED' },
  { label: 'Yêu Cầu Hoàn Trả', value: 'RETURN_REQUESTED' },
  { label: 'Đã Trả Hàng', value: 'RETURNED' },
];

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [keyword, setKeyword] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);

  // Status Update Modal
  const [updatingOrder, setUpdatingOrder] = useState<OrderResponse | null>(null);
  const [newStatus, setNewStatus] = useState<string>('CONFIRMED');
  const [trackingNumber, setTrackingNumber] = useState<string>('');
  const [carrier, setCarrier] = useState<string>('SPX Express');
  const [statusNote, setStatusNote] = useState<string>('');
  const [isUpdatingStatus, setIsUpdatingStatus] = useState<boolean>(false);

  // Staff Notes Modal
  const [staffNoteOrder, setStaffNoteOrder] = useState<OrderResponse | null>(null);
  const [staffNotesText, setStaffNotesText] = useState<string>('');
  const [isSavingNotes, setIsSavingNotes] = useState<boolean>(false);

  // Detail Modal
  const [detailOrder, setDetailOrder] = useState<OrderResponse | null>(null);

  const fetchOrders = async () => {
    setIsLoading(true);
    try {
      const res = await orderService.getAdminOrders({
        keyword: keyword.trim() || undefined,
        status: selectedStatus || undefined,
        page: 0,
        size: 50,
      });
      setOrders(res.content || []);
    } catch (e) {
      console.error('Failed to load admin orders:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [selectedStatus]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchOrders();
  };

  const handleExportExcel = async () => {
    setIsExporting(true);
    try {
      await orderService.exportOrdersExcel({ status: selectedStatus || undefined });
      toast.success('Đã xuất báo cáo Excel thành công!', { icon: '📊' });
    } catch (e) {
      toast.error('Không thể xuất Excel lúc này');
    } finally {
      setIsExporting(false);
    }
  };

  const openStatusModal = (order: OrderResponse) => {
    setUpdatingOrder(order);
    // Suggest next logical status in State Machine
    const s = order.status;
    if (s === 'PENDING' || s === 'PENDING_PAYMENT') setNewStatus('CONFIRMED');
    else if (s === 'CONFIRMED') setNewStatus('PREPARING');
    else if (s === 'PROCESSING' || s === 'PREPARING') setNewStatus('SHIPPING');
    else if (s === 'SHIPPED' || s === 'SHIPPING') setNewStatus('DELIVERED');
    else if (s === 'DELIVERED') setNewStatus('COMPLETED');
    else if (s === 'RETURN_REQUESTED') setNewStatus('RETURNED');
    else setNewStatus('CONFIRMED');

    setTrackingNumber(order.trackingNumber || '');
    setCarrier(order.carrier || 'SPX Express');
    setStatusNote('');
  };

  const handleStatusSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!updatingOrder) return;
    setIsUpdatingStatus(true);
    try {
      await orderService.updateOrderStatus(updatingOrder.id, {
        newStatus,
        trackingNumber: trackingNumber.trim() || undefined,
        carrier: carrier.trim() || undefined,
        note: statusNote.trim() || undefined,
      });
      toast.success(`Đã cập nhật trạng thái đơn ${updatingOrder.orderCode} sang ${newStatus}`);
      setUpdatingOrder(null);
      fetchOrders();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể đổi trạng thái đơn hàng');
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  const openStaffNoteModal = (order: OrderResponse) => {
    setStaffNoteOrder(order);
    setStaffNotesText(order.staffNotes || '');
  };

  const handleStaffNoteSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!staffNoteOrder) return;
    setIsSavingNotes(true);
    try {
      await orderService.updateStaffNotes(staffNoteOrder.id, staffNotesText.trim());
      toast.success('Đã lưu ghi chú nội bộ!');
      setStaffNoteOrder(null);
      fetchOrders();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể lưu ghi chú');
    } finally {
      setIsSavingNotes(false);
    }
  };

  const handleDownloadPdf = async (order: OrderResponse) => {
    try {
      await orderService.downloadInvoicePdf(order.id, order.orderCode);
      toast.success('Đã xuất hóa đơn PDF thành công!');
    } catch (e) {
      toast.error('Không thể xuất hóa đơn PDF');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="font-serif text-2xl lg:text-3xl font-bold text-primary">
              Quản Lý Đơn Hàng & Vận Hành Bán Lẻ
            </h1>
            <p className="text-xs text-text-wood-muted mt-0.5">
              Duyệt trạng thái đơn theo State Machine, gắn mã vận đơn và xuất báo cáo kế toán
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleExportExcel}
              disabled={isExporting}
              className="px-4 py-2 rounded-xl bg-primary text-secondary-fixed text-xs font-bold hover:bg-primary-container transition-colors flex items-center gap-1.5 shadow-sm disabled:opacity-50"
            >
              <span className="material-symbols-outlined text-[16px]">table_view</span>
              <span>{isExporting ? 'Đang xuất...' : 'Xuất Excel'}</span>
            </button>
          </div>
        </div>

        {/* Filter Strip */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pt-2">
          {/* Status Tabs */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
            {STATUS_FILTERS.map((f) => (
              <button
                key={f.value}
                onClick={() => setSelectedStatus(f.value)}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                  selectedStatus === f.value
                    ? 'bg-primary text-secondary-fixed shadow-sm'
                    : 'bg-surface-container-low text-text-wood-muted hover:bg-surface-container'
                }`}
              >
                {f.label}
              </button>
            ))}
          </div>

          {/* Search Box */}
          <form onSubmit={handleSearchSubmit} className="relative flex-1 max-w-xs">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-wood-muted text-[18px]">
              search
            </span>
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Tìm theo mã đơn hoặc SĐT..."
              className="w-full pl-9 pr-3 py-1.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary font-mono"
            />
          </form>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-bg-warm-sand/80 text-text-wood-muted uppercase font-mono border-b border-border-bamboo">
              <tr>
                <th className="p-4">Mã Đơn</th>
                <th className="p-4">Khách Hàng</th>
                <th className="p-4">Tổng Tiền</th>
                <th className="p-4">Thanh Toán</th>
                <th className="p-4">Trạng Thái Đơn</th>
                <th className="p-4">Ngày Đặt</th>
                <th className="p-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-bamboo/50">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-text-wood-muted">
                    <span className="material-symbols-outlined text-[32px] text-primary animate-spin">spa</span>
                    <p className="mt-2 font-serif text-primary">Đang tải danh sách đơn hàng...</p>
                  </td>
                </tr>
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-text-wood-muted">
                    Chưa có đơn hàng nào khớp với điều kiện lọc.
                  </td>
                </tr>
              ) : (
                orders.map((order) => {
                  const statusConfig = getOrderStatusInfo(order.status);

                  return (
                    <tr key={order.id} className="hover:bg-surface-container-low transition-colors">
                      <td className="p-4">
                        <span className="font-mono font-bold text-primary bg-bg-warm-sand px-2 py-0.5 rounded border border-border-bamboo">
                          {order.orderCode}
                        </span>
                      </td>

                      <td className="p-4">
                        <span className="font-bold text-text-ink-black block">
                          {order.shippingAddress?.recipientName}
                        </span>
                        <span className="font-mono text-text-wood-muted text-[11px]">
                          {order.shippingAddress?.phoneNumber}
                        </span>
                      </td>

                      <td className="p-4">
                        <span className="font-mono font-bold text-primary text-sm">
                          {formatCurrency(order.totalAmount)}
                        </span>
                        <span className="text-[11px] text-text-wood-muted block">
                          {order.items?.length || 1} món
                        </span>
                      </td>

                      <td className="p-4">
                        <div className="flex flex-col gap-0.5">
                          <span className="font-mono text-[11px] font-bold text-primary">
                            {order.paymentMethod}
                          </span>
                          <span
                            className={`text-[10px] font-semibold ${
                              order.paymentStatus === 'PAID' ? 'text-accent-lotus-jade' : 'text-text-wood-muted'
                            }`}
                          >
                            {order.paymentStatus === 'PAID' ? '✓ Đã Thanh Toán' : 'Chưa Thanh Toán'}
                          </span>
                        </div>
                      </td>

                      <td className="p-4">
                        <span className={`px-2.5 py-1 rounded-full text-[11px] font-semibold inline-flex items-center gap-1 ${statusConfig.bg} ${statusConfig.text}`}>
                          <span className="material-symbols-outlined text-[13px]">{statusConfig.icon}</span>
                          <span>{statusConfig.label}</span>
                        </span>
                      </td>

                      <td className="p-4 text-text-wood-muted text-[11px]">
                        {formatDate(order.createdAt)}
                      </td>

                      <td className="p-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => openStatusModal(order)}
                            className="px-2.5 py-1 rounded-lg bg-primary text-secondary-fixed text-[11px] font-semibold hover:bg-primary-container transition-colors shadow-sm"
                            title="Đổi trạng thái"
                          >
                            Đổi Trạng Thái
                          </button>

                          <button
                            onClick={() => openStaffNoteModal(order)}
                            className="p-1 rounded-lg bg-surface border border-border-bamboo text-text-wood-muted hover:text-primary transition-colors"
                            title="Ghi chú nhân viên"
                          >
                            <span className="material-symbols-outlined text-[16px]">edit_note</span>
                          </button>

                          <button
                            onClick={() => handleDownloadPdf(order)}
                            className="p-1 rounded-lg bg-surface border border-border-bamboo text-text-wood-muted hover:text-primary transition-colors"
                            title="In hóa đơn PDF"
                          >
                            <span className="material-symbols-outlined text-[16px]">download</span>
                          </button>

                          <button
                            onClick={() => setDetailOrder(order)}
                            className="p-1 rounded-lg bg-surface border border-border-bamboo text-text-wood-muted hover:text-primary transition-colors"
                            title="Xem chi tiết"
                          >
                            <span className="material-symbols-outlined text-[16px]">visibility</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal 1: State Machine Status Transition */}
      {updatingOrder && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 sm:p-8 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
              <h3 className="font-serif text-lg font-bold text-primary">
                Cập Nhật Trạng Thái Đơn {updatingOrder.orderCode}
              </h3>
              <button onClick={() => setUpdatingOrder(null)} className="text-text-wood-muted p-1">
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            <form onSubmit={handleStatusSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                  Trạng Thái Mới (State Machine)
                </label>
                <select
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value)}
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary font-semibold"
                >
                  <option value="CONFIRMED">CONFIRMED — Đã Xác Nhận</option>
                  <option value="PREPARING">PREPARING — Đang Chuẩn Bị & Đóng Gói</option>
                  <option value="SHIPPING">SHIPPING — Đang Vận Chuyển Giao Hàng</option>
                  <option value="DELIVERED">DELIVERED — Đã Giao Thành Công</option>
                  <option value="COMPLETED">COMPLETED — Hoàn Tất Đơn Hàng</option>
                  <option value="RETURNED">RETURNED — Đã Trả Hàng & Hoàn Tiền</option>
                  <option value="CANCELLED">CANCELLED — Hủy Đơn Hàng</option>
                </select>
              </div>

              {(newStatus === 'SHIPPING' || newStatus === 'SHIPPED') && (
                <>
                  <div>
                    <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                      Mã Vận Đơn (Tracking Number) *
                    </label>
                    <input
                      type="text"
                      value={trackingNumber}
                      onChange={(e) => setTrackingNumber(e.target.value)}
                      placeholder="VD: SPXVN09823412"
                      required
                      className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs font-mono text-text-ink-black focus:outline-none focus:border-primary"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                      Đơn Vị Vận Chuyển (Carrier)
                    </label>
                    <input
                      type="text"
                      value={carrier}
                      onChange={(e) => setCarrier(e.target.value)}
                      placeholder="SPX Express / Giao Hàng Tiết Kiệm..."
                      className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                    />
                  </div>
                </>
              )}

              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                  Ghi Chú Tiến Trình
                </label>
                <input
                  type="text"
                  value={statusNote}
                  onChange={(e) => setStatusNote(e.target.value)}
                  placeholder="Ghi chú thêm về việc giao nhận..."
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-bamboo">
                <button
                  type="button"
                  onClick={() => setUpdatingOrder(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  disabled={isUpdatingStatus}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-bold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isUpdatingStatus ? 'Đang lưu...' : 'Lưu Thay Đổi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 2: Staff Internal Notes */}
      {staffNoteOrder && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 shadow-2xl space-y-4">
            <h3 className="font-serif text-lg font-bold text-primary">
              Ghi Chú Nội Bộ — Đơn {staffNoteOrder.orderCode}
            </h3>
            <p className="text-xs text-text-wood-muted">
              Ghi chú này chỉ hiển thị cho nhân viên (STAFF) và quản trị viên (ADMIN), khách hàng không nhìn thấy.
            </p>
            <form onSubmit={handleStaffNoteSubmit} className="space-y-4">
              <textarea
                rows={4}
                value={staffNotesText}
                onChange={(e) => setStaffNotesText(e.target.value)}
                placeholder="Ghi chú về đóng gói, khách hàng VIP, thời gian liên hệ..."
                className="w-full p-3 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
              />

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setStaffNoteOrder(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  disabled={isSavingNotes}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-bold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isSavingNotes ? 'Đang lưu...' : 'Lưu Ghi Chú'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 3: Order Full Detail Drawer */}
      {detailOrder && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-2xl max-h-[85vh] overflow-y-auto rounded-3xl border border-border-bamboo p-6 sm:p-8 shadow-2xl space-y-6">
            <div className="flex items-center justify-between pb-3 border-b border-border-bamboo">
              <div>
                <span className="text-xs font-mono uppercase text-text-wood-muted">Chi Tiết Đơn Hàng</span>
                <h3 className="font-serif text-xl font-bold text-primary">{detailOrder.orderCode}</h3>
              </div>
              <button onClick={() => setDetailOrder(null)} className="text-text-wood-muted p-1">
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            {/* Customer & Shipping Details */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 bg-bg-warm-sand/50 p-4 rounded-2xl border border-border-bamboo text-xs">
              <div>
                <span className="text-text-wood-muted uppercase font-bold text-[10px] block">Người Nhận:</span>
                <p className="font-semibold text-text-ink-black">{detailOrder.shippingAddress?.recipientName}</p>
                <p className="font-mono text-text-wood-muted">{detailOrder.shippingAddress?.phoneNumber}</p>
              </div>
              <div>
                <span className="text-text-wood-muted uppercase font-bold text-[10px] block">Địa Chỉ:</span>
                <p className="text-text-ink-black leading-relaxed">
                  {detailOrder.shippingAddress?.streetAddress}, {detailOrder.shippingAddress?.ward},{' '}
                  {detailOrder.shippingAddress?.district}, {detailOrder.shippingAddress?.province}
                </p>
              </div>
            </div>

            {/* Items */}
            <div className="space-y-3">
              <span className="font-serif font-bold text-primary text-sm block">Vật Phẩm Trong Đơn:</span>
              <div className="divide-y divide-border-bamboo/40 border border-border-bamboo rounded-2xl p-4 bg-surface">
                {detailOrder.items?.map((item) => (
                  <div key={item.id} className="py-2.5 first:pt-0 last:pb-0 flex items-center justify-between gap-3 text-xs">
                    <div>
                      <p className="font-bold text-text-ink-black">{item.productName}</p>
                      <p className="text-text-wood-muted text-[11px]">{item.variantName} • SKU: {item.sku}</p>
                      <span className="font-mono text-text-wood-muted">{formatCurrency(item.unitPrice)} × {item.quantity}</span>
                    </div>
                    <span className="font-mono font-bold text-primary">{formatCurrency(item.totalPrice)}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Total */}
            <div className="flex items-baseline justify-between pt-3 border-t border-border-bamboo">
              <span className="font-serif font-bold text-base text-primary">Tổng Tiền Đơn Hàng:</span>
              <span className="font-mono text-xl font-bold text-primary">{formatCurrency(detailOrder.totalAmount)}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

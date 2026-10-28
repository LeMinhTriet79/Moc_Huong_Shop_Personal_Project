import api from '@/lib/axios';
import {
  ApiResponse,
  CheckoutRequest,
  CouponCalculationResult,
  CouponResponse,
  DashboardStatsResponse,
  OrderResponse,
  Page,
} from '@/types';

export const orderService = {
  // Coupons (Public & Customer)
  async getActiveCoupons(): Promise<CouponResponse[]> {
    const res = await api.get<ApiResponse<CouponResponse[]>>('/api/order/coupons/active');
    return res.data.data || [];
  },

  async previewCoupon(
    couponCode: string,
    subtotal: number,
    shippingFee: number = 30000
  ): Promise<CouponCalculationResult> {
    const res = await api.post<ApiResponse<CouponCalculationResult>>('/api/order/coupons/preview', {
      couponCode,
      subtotal,
      shippingFee,
    });
    return res.data.data;
  },

  // Checkout & Orders
  async checkout(request: CheckoutRequest): Promise<OrderResponse> {
    const res = await api.post<ApiResponse<OrderResponse>>('/api/order/orders/checkout', request);
    return res.data.data;
  },

  async getMyOrders(status?: string, page: number = 0, size: number = 10): Promise<Page<OrderResponse>> {
    const res = await api.get<ApiResponse<Page<OrderResponse>>>('/api/order/orders', {
      params: { status: status === 'ALL' ? undefined : status, page, size },
    });
    return res.data.data;
  },

  async getOrderByCode(orderCode: string): Promise<OrderResponse> {
    const res = await api.get<ApiResponse<OrderResponse>>(`/api/order/orders/${orderCode}`);
    return res.data.data;
  },

  async cancelOrder(orderId: number, reason: string): Promise<OrderResponse> {
    const res = await api.put<ApiResponse<OrderResponse>>(`/api/order/orders/${orderId}/cancel`, {
      reason,
    });
    return res.data.data;
  },

  async requestReturn(orderId: number, reason: string): Promise<OrderResponse> {
    const res = await api.post<ApiResponse<OrderResponse>>(`/api/order/orders/${orderId}/return-request`, {
      reason,
    });
    return res.data.data;
  },

  async downloadInvoicePdf(orderId: number, orderCode?: string): Promise<void> {
    const response = await api.get(`/api/order/orders/${orderId}/invoice-pdf`, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Hoa_don_Moc_Huong_${orderCode || orderId}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  async createVNPayPayment(orderId: number, amount?: number, orderInfo?: string): Promise<{ paymentUrl: string }> {
    const res = await api.get<ApiResponse<{ paymentUrl: string }>>(`/api/order/payment/create-url/${orderId}`, {
      params: { amount, orderInfo },
    });
    return res.data.data;
  },

  // STAFF / ADMIN APIs
  async getAdminOrders(params?: {
    keyword?: string;
    status?: string;
    paymentStatus?: string;
    paymentMethod?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }): Promise<Page<OrderResponse>> {
    const res = await api.get<ApiResponse<Page<OrderResponse>>>('/api/order/admin/orders', { params });
    return res.data.data;
  },

  async getAdminOrderById(orderId: number): Promise<OrderResponse> {
    const res = await api.get<ApiResponse<OrderResponse>>(`/api/order/admin/orders/${orderId}`);
    return res.data.data;
  },

  async updateOrderStatus(
    orderId: number,
    data: {
      newStatus: string;
      trackingNumber?: string;
      carrier?: string;
      note?: string;
    }
  ): Promise<OrderResponse> {
    const res = await api.put<ApiResponse<OrderResponse>>(`/api/order/admin/orders/${orderId}/status`, data);
    return res.data.data;
  },

  async updateStaffNotes(orderId: number, staffNotes: string): Promise<OrderResponse> {
    const res = await api.put<ApiResponse<OrderResponse>>(`/api/order/admin/orders/${orderId}/staff-notes`, {
      staffNotes,
    });
    return res.data.data;
  },

  async exportOrdersExcel(params?: { startDate?: string; endDate?: string; status?: string }): Promise<void> {
    const response = await api.get('/api/order/admin/orders/export-excel', {
      params,
      responseType: 'blob',
    });
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Bao_cao_don_hang_Moc_Huong_${new Date().toISOString().slice(0, 10)}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  async getDashboardStatistics(startDate?: string, endDate?: string): Promise<DashboardStatsResponse> {
    const res = await api.get<ApiResponse<DashboardStatsResponse>>('/api/order/admin/orders/statistics', {
      params: { startDate, endDate },
    });
    return res.data.data;
  },

  async getAdminCoupons(page: number = 0, size: number = 20): Promise<Page<CouponResponse>> {
    const res = await api.get<ApiResponse<Page<CouponResponse>>>('/api/order/admin/coupons', {
      params: { page, size },
    });
    return res.data.data;
  },

  async createCoupon(data: any): Promise<CouponResponse> {
    const res = await api.post<ApiResponse<CouponResponse>>('/api/order/admin/coupons', data);
    return res.data.data;
  },

  async updateCoupon(id: number, data: any): Promise<CouponResponse> {
    const res = await api.put<ApiResponse<CouponResponse>>(`/api/order/admin/coupons/${id}`, data);
    return res.data.data;
  },

  async deleteCoupon(id: number): Promise<void> {
    await api.delete(`/api/order/admin/coupons/${id}`);
  },
};

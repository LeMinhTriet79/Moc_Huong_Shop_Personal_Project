import api from '@/lib/axios';
import {
  ApiResponse,
  CategoryResponse,
  InventoryResponse,
  Page,
  ProductResponse,
  ProductVariantResponse,
  ReviewCreateRequest,
  ReviewResponse,
} from '@/types';

export const catalogService = {
  // Public Catalog
  async getProducts(params?: {
    keyword?: string;
    categoryId?: number;
    brand?: string;
    minPrice?: number;
    maxPrice?: number;
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: string;
  }): Promise<Page<ProductResponse>> {
    const res = await api.get<ApiResponse<Page<ProductResponse>>>('/api/catalog/products', { params });
    return res.data.data;
  },

  async getFeaturedProducts(): Promise<ProductResponse[]> {
    const res = await api.get<ApiResponse<ProductResponse[]>>('/api/catalog/products/featured');
    return res.data.data || [];
  },

  async getProductById(id: number | string): Promise<ProductResponse> {
    const res = await api.get<ApiResponse<ProductResponse>>(`/api/catalog/products/${id}`);
    return res.data.data;
  },

  async getCategories(): Promise<CategoryResponse[]> {
    const res = await api.get<ApiResponse<CategoryResponse[]>>('/api/catalog/categories');
    return res.data.data || [];
  },

  async getVariants(productId: number): Promise<ProductVariantResponse[]> {
    const res = await api.get<ApiResponse<ProductVariantResponse[]>>(
      `/api/catalog/products/${productId}/variants`
    );
    return res.data.data || [];
  },

  async getInventory(variantId: number): Promise<InventoryResponse> {
    const res = await api.get<ApiResponse<InventoryResponse>>(`/api/catalog/inventory/variant/${variantId}`);
    return res.data.data;
  },

  async getReviews(
    productId: number,
    page: number = 0,
    size: number = 10
  ): Promise<Page<ReviewResponse>> {
    const res = await api.get<ApiResponse<Page<ReviewResponse>>>(
      `/api/catalog/reviews/product/${productId}`,
      { params: { page, size } }
    );
    return res.data.data;
  },

  async createReview(data: ReviewCreateRequest): Promise<ReviewResponse> {
    const res = await api.post<ApiResponse<ReviewResponse>>('/api/catalog/reviews', data);
    return res.data.data;
  },

  // STAFF / ADMIN APIs
  async getInactiveProducts(page: number = 0, size: number = 10): Promise<Page<ProductResponse>> {
    const res = await api.get<ApiResponse<Page<ProductResponse>>>('/api/catalog/products/inactive', {
      params: { page, size },
    });
    return res.data.data;
  },

  async createProduct(formData: FormData): Promise<ProductResponse> {
    const res = await api.post<ApiResponse<ProductResponse>>('/api/catalog/products', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },

  async updateProduct(id: number, formData: FormData): Promise<ProductResponse> {
    const res = await api.put<ApiResponse<ProductResponse>>(`/api/catalog/products/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },

  async deleteProduct(id: number): Promise<void> {
    await api.delete(`/api/catalog/products/${id}`);
  },

  async restoreProduct(id: number): Promise<ProductResponse> {
    const res = await api.put<ApiResponse<ProductResponse>>(`/api/catalog/products/${id}/restore`);
    return res.data.data;
  },

  async forceDeleteProduct(id: number): Promise<void> {
    await api.delete(`/api/catalog/products/${id}/force`);
  },

  async addVariant(productId: number, data: any): Promise<ProductVariantResponse> {
    const res = await api.post<ApiResponse<ProductVariantResponse>>(
      `/api/catalog/products/${productId}/variants`,
      data
    );
    return res.data.data;
  },

  async updateVariant(productId: number, variantId: number, data: any): Promise<ProductVariantResponse> {
    const res = await api.put<ApiResponse<ProductVariantResponse>>(
      `/api/catalog/products/${productId}/variants/${variantId}`,
      data
    );
    return res.data.data;
  },

  async deleteVariant(productId: number, variantId: number): Promise<void> {
    await api.delete(`/api/catalog/products/${productId}/variants/${variantId}`);
  },

  async updateInventory(variantId: number, data: { quantity: number; lowStockThreshold: number }): Promise<InventoryResponse> {
    const res = await api.put<ApiResponse<InventoryResponse>>(
      `/api/catalog/inventory/variant/${variantId}`,
      data
    );
    return res.data.data;
  },

  async getLowStockInventory(): Promise<InventoryResponse[]> {
    const res = await api.get<ApiResponse<InventoryResponse[]>>('/api/catalog/inventory/low-stock');
    return res.data.data || [];
  },

  async getAdminReviews(params?: {
    productId?: number;
    rating?: number;
    isVisible?: boolean;
    page?: number;
    size?: number;
  }): Promise<Page<ReviewResponse>> {
    const res = await api.get<ApiResponse<Page<ReviewResponse>>>('/api/catalog/reviews/admin/reviews', {
      params,
    });
    return res.data.data;
  },

  async replyReview(reviewId: number, replyMessage: string): Promise<ReviewResponse> {
    const res = await api.put<ApiResponse<ReviewResponse>>(
      `/api/catalog/reviews/${reviewId}/reply`,
      { replyMessage }
    );
    return res.data.data;
  },

  async toggleReviewVisibility(reviewId: number): Promise<ReviewResponse> {
    const res = await api.put<ApiResponse<ReviewResponse>>(
      `/api/catalog/reviews/${reviewId}/toggle-visibility`
    );
    return res.data.data;
  },
};

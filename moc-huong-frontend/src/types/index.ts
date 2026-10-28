// Common API Response structure
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  errorCode: string | null;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// User & Auth
export type UserRole = 'CUSTOMER' | 'STAFF' | 'ADMIN';

export interface UserResponse {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string;
  avatarUrl?: string;
  dateOfBirth?: string;
  role: UserRole;
  provider: 'LOCAL' | 'GOOGLE';
  isEmailVerified: boolean;
  isActive: boolean;
  createdAt: string;
  addresses?: AddressResponse[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  fullName: string;
  role: UserRole;
}

export interface AddressResponse {
  id: number;
  userId?: number;
  recipientName: string;
  phoneNumber: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  isDefault: boolean;
}

export interface AddressRequest {
  recipientName: string;
  phoneNumber: string;
  province: string;
  district: string;
  ward: string;
  streetAddress: string;
  isDefault?: boolean;
}

// Wishlist
export interface WishlistResponse {
  id: number;
  userId: number;
  productId: number;
  createdAt?: string;
  product?: ProductResponse;
}

// Catalog & Products
export interface CategoryResponse {
  id: number;
  parentId?: number | null;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  isActive: boolean;
  children?: CategoryResponse[];
}

export interface ProductImageResponse {
  id: number;
  productId: number;
  imageUrl: string;
  altText?: string;
  displayOrder: number;
  isPrimary: boolean;
}

export interface ProductVariantResponse {
  id: number;
  productId: number;
  sku: string;
  variantName: string;
  price: number;
  weightGrams: number;
  attributes?: Record<string, any>;
  isActive: boolean;
  stockQuantity?: number;
  availableStock?: number;
}

export interface ProductResponse {
  id: number;
  categoryId: number;
  categoryName?: string;
  name: string;
  slug: string;
  skuPrefix: string;
  description: string;
  brand: string;
  basePrice: number;
  startingPrice?: number;
  tags?: string[] | string;
  attributes?: Record<string, any>;
  isActive: boolean;
  images: ProductImageResponse[];
  variants: ProductVariantResponse[];
  averageRating?: number;
  reviewCount?: number;
  totalSold?: number;
}

export interface InventoryResponse {
  id?: number;
  variantId: number;
  sku?: string;
  quantity: number;
  reservedQuantity: number;
  availableStock: number;
  lowStockThreshold: number;
  isLowStock?: boolean;
  productName?: string;
  variantName?: string;
}

export interface ReviewResponse {
  id: number;
  productId: number;
  productName?: string;
  userId: number;
  orderId?: number;
  userName?: string;
  userAvatar?: string;
  rating: number;
  comment: string;
  content?: string;
  replyMessage?: string;
  adminReply?: string;
  repliedAt?: string;
  isVerifiedPurchase: boolean;
  isVisible: boolean;
  createdAt: string;
}

export interface ReviewCreateRequest {
  productId: number;
  orderId?: number;
  rating: number;
  comment: string;
}

// Order & Cart
export interface CartItem {
  productId: number;
  variantId: number;
  sku?: string;
  productName: string;
  variantName: string;
  productImageUrl: string;
  price: number;
  quantity: number;
  availableStock?: number;
}

export interface CouponResponse {
  id: number;
  code: string;
  title: string;
  name?: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING';
  type?: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING';
  discountValue: number;
  value?: number;
  minOrderValue: number;
  maxDiscountAmount?: number;
  startDate: string;
  startsAt?: string;
  endDate: string;
  expiresAt?: string;
  usageLimit?: number;
  maxUses?: number;
  usedCount: number;
  isActive: boolean;
}

export interface CouponCalculationResult {
  valid: boolean;
  discountAmount: number;
  finalTotal: number;
  message: string;
  couponCode?: string;
}

export type OrderStatus =
  | 'PENDING'
  | 'PENDING_PAYMENT'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'PREPARING'
  | 'SHIPPED'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'RETURN_REQUESTED'
  | 'RETURNED';

export type PaymentMethod = 'COD' | 'VNPAY';
export type PaymentStatus = 'UNPAID' | 'PAID' | 'FAILED' | 'REFUNDED';

export interface OrderShippingAddress {
  recipientName: string;
  phoneNumber: string;
  streetAddress: string;
  ward: string;
  district: string;
  province: string;
}

export interface OrderItemResponse {
  id: number;
  orderId: number;
  productId: number;
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  productImageUrl: string;
  unitPrice: number;
  discountAmount: number;
  quantity: number;
  totalPrice: number;
}

export interface OrderStatusHistoryResponse {
  id: number;
  orderId: number;
  fromStatus?: string;
  toStatus: string;
  changedBy: string;
  changedByRole: string;
  note?: string;
  createdAt: string;
}

export interface OrderTransactionResponse {
  id: number;
  orderId: number;
  transactionReference: string;
  gatewayTransactionNo?: string;
  amount: number;
  status: string;
  bankCode?: string;
  cardType?: string;
  createdAt: string;
}

export interface OrderResponse {
  id: number;
  orderCode: string;
  idempotencyKey?: string;
  userId: number;
  status: OrderStatus;
  currency: string;
  subtotal: number;
  shippingFee: number;
  discountAmount: number;
  totalAmount: number;
  couponCode?: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  shippingAddress: OrderShippingAddress;
  trackingNumber?: string;
  carrier?: string;
  notes?: string;
  staffNotes?: string;
  cancelReason?: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemResponse[];
  statusHistory?: OrderStatusHistoryResponse[];
  transactions?: OrderTransactionResponse[];
}

export interface CheckoutRequest {
  shippingAddress: OrderShippingAddress;
  paymentMethod: PaymentMethod;
  couponCode?: string;
  notes?: string;
  idempotencyKey: string;
}

// Admin Dashboard KPI
export interface DashboardStatsResponse {
  totalRevenue: number;
  totalOrders: number;
  averageOrderValue: number;
  totalCustomers: number;
  revenueByDate: { date: string; amount: number }[];
  ordersByStatus: Record<string, number>;
  topProducts?: {
    productId: number;
    productName: string;
    totalQuantity: number;
    totalRevenue: number;
  }[];
}

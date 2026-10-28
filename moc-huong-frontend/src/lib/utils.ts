import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { OrderStatus } from '@/types';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Currency format for Vietnamese Dong: 350.000 ₫
export function formatCurrency(amount: number | string | undefined | null): string {
  if (amount === undefined || amount === null) return '0 ₫';
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(num)) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(num);
}

// Format date: 26/10/2026, 14:30
export function formatDate(dateString: string | undefined | null): string {
  if (!dateString) return '';
  try {
    const d = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    }).format(d);
  } catch {
    return dateString;
  }
}

// Order Status Helpers
export const ORDER_STATUS_MAP: Record<
  OrderStatus,
  { label: string; bg: string; text: string; icon: string }
> = {
  PENDING: {
    label: 'Chờ thanh toán',
    bg: 'bg-secondary-fixed/40',
    text: 'text-secondary-fixed-variant',
    icon: 'hourglass_empty',
  },
  PENDING_PAYMENT: {
    label: 'Chờ thanh toán',
    bg: 'bg-secondary-fixed/40',
    text: 'text-secondary-fixed-variant',
    icon: 'hourglass_empty',
  },
  CONFIRMED: {
    label: 'Đã xác nhận',
    bg: 'bg-tertiary-fixed/40',
    text: 'text-tertiary',
    icon: 'check_circle',
  },
  PROCESSING: {
    label: 'Đang chuẩn bị hàng',
    bg: 'bg-primary-fixed/50',
    text: 'text-primary',
    icon: 'package_2',
  },
  PREPARING: {
    label: 'Đang chuẩn bị hàng',
    bg: 'bg-primary-fixed/50',
    text: 'text-primary',
    icon: 'package_2',
  },
  SHIPPED: {
    label: 'Đang vận chuyển',
    bg: 'bg-secondary-container/50',
    text: 'text-on-secondary-container',
    icon: 'local_shipping',
  },
  SHIPPING: {
    label: 'Đang vận chuyển',
    bg: 'bg-secondary-container/50',
    text: 'text-on-secondary-container',
    icon: 'local_shipping',
  },
  DELIVERED: {
    label: 'Đã giao thành công',
    bg: 'bg-accent-lotus-jade/15',
    text: 'text-accent-lotus-jade',
    icon: 'task_alt',
  },
  COMPLETED: {
    label: 'Hoàn tất',
    bg: 'bg-accent-lotus-jade/20',
    text: 'text-accent-lotus-jade',
    icon: 'verified',
  },
  CANCELLED: {
    label: 'Đã hủy đơn',
    bg: 'bg-status-cinnabar/15',
    text: 'text-status-cinnabar',
    icon: 'cancel',
  },
  RETURN_REQUESTED: {
    label: 'Yêu cầu trả hàng',
    bg: 'bg-outline-variant/30',
    text: 'text-text-wood-muted',
    icon: 'assignment_return',
  },
  RETURNED: {
    label: 'Đã trả hàng / Hoàn tiền',
    bg: 'bg-outline-variant/30',
    text: 'text-text-wood-muted',
    icon: 'assignment_return',
  },
};

export function getOrderStatusInfo(status?: string): { label: string; bg: string; text: string; icon: string } {
  if (!status) {
    return {
      label: 'Không xác định',
      bg: 'bg-surface-container',
      text: 'text-text-wood-muted',
      icon: 'help_outline',
    };
  }
  const normalized = status.toUpperCase() as OrderStatus;
  return (
    ORDER_STATUS_MAP[normalized] || {
      label: status,
      bg: 'bg-surface-container',
      text: 'text-text-wood-muted',
      icon: 'info',
    }
  );
}

'use client';

import React, { useState, useEffect } from 'react';
import { catalogService } from '@/services/catalogService';
import { ReviewResponse } from '@/types';
import { formatDate } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function AdminReviewsPage() {
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Reply Modal State
  const [replyReviewItem, setReplyReviewItem] = useState<ReviewResponse | null>(null);
  const [replyMessage, setReplyMessage] = useState('');
  const [isReplying, setIsReplying] = useState(false);

  const loadReviews = async () => {
    setIsLoading(true);
    try {
      const res = await catalogService.getAdminReviews({ size: 50 });
      setReviews(res.content || []);
    } catch (e) {
      console.error('Failed to load reviews:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadReviews();
  }, []);

  const handleToggleVisibility = async (id: number) => {
    try {
      await catalogService.toggleReviewVisibility(id);
      toast.success('Đã cập nhật trạng thái hiển thị đánh giá');
      loadReviews();
    } catch (e) {
      toast.error('Không thể thay đổi trạng thái');
    }
  };

  const openReplyModal = (rev: ReviewResponse) => {
    setReplyReviewItem(rev);
    setReplyMessage(rev.replyMessage || rev.adminReply || '');
  };

  const handleReplySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!replyReviewItem || !replyMessage.trim()) return;
    setIsReplying(true);
    try {
      await catalogService.replyReview(replyReviewItem.id, replyMessage.trim());
      toast.success('Đã lưu phản hồi cho khách hàng!');
      setReplyReviewItem(null);
      loadReviews();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể lưu phản hồi');
    } finally {
      setIsReplying(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-surface rounded-3xl p-6 sm:p-8 border border-border-bamboo shadow-sm space-y-2">
        <h1 className="font-serif text-2xl lg:text-3xl font-bold text-primary">
          Kiểm Duyệt & Phản Hồi Đánh Giá Khách Hàng
        </h1>
        <p className="text-xs text-text-wood-muted">
          Quản lý cảm nhận của khách hàng, phản hồi chính thức từ Am Trầm và ẩn các bình luận không phù hợp
        </p>
      </div>

      {/* Reviews Table */}
      <div className="bg-surface rounded-3xl border border-border-bamboo shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-bg-warm-sand/80 text-text-wood-muted uppercase font-mono border-b border-border-bamboo">
              <tr>
                <th className="p-4">Khách Hàng</th>
                <th className="p-4">Số Sao</th>
                <th className="p-4">Nội Dung Đánh Giá</th>
                <th className="p-4">Phản Hồi Từ Shop</th>
                <th className="p-4">Trạng Thái</th>
                <th className="p-4">Ngày Gửi</th>
                <th className="p-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-bamboo/50">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-text-wood-muted">
                    <span className="material-symbols-outlined text-[32px] text-primary animate-spin">spa</span>
                    <p className="mt-2 font-serif text-primary">Đang tải danh sách đánh giá...</p>
                  </td>
                </tr>
              ) : reviews.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-text-wood-muted">
                    Chưa có đánh giá nào cần kiểm duyệt.
                  </td>
                </tr>
              ) : (
                reviews.map((rev) => (
                  <tr key={rev.id} className="hover:bg-surface-container-low transition-colors">
                    <td className="p-4">
                      <span className="font-bold text-text-ink-black block">
                        {rev.userName || `User #${rev.userId}`}
                      </span>
                      {rev.isVerifiedPurchase && (
                        <span className="text-[10px] text-accent-lotus-jade font-semibold flex items-center gap-0.5">
                          <span className="material-symbols-outlined text-[12px]">verified</span>
                          Đã mua hàng
                        </span>
                      )}
                    </td>

                    <td className="p-4">
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
                    </td>

                    <td className="p-4 max-w-xs">
                      <p className="text-text-ink-black leading-relaxed line-clamp-2">
                        {rev.comment || rev.content}
                      </p>
                    </td>

                    <td className="p-4 max-w-xs">
                      {rev.replyMessage || rev.adminReply ? (
                        <p className="text-text-wood-muted italic line-clamp-2 text-[11px] bg-bg-warm-sand/50 p-2 rounded-lg border border-border-bamboo/60">
                          {rev.replyMessage || rev.adminReply}
                        </p>
                      ) : (
                        <span className="text-text-wood-muted text-[11px] italic">Chưa phản hồi</span>
                      )}
                    </td>

                    <td className="p-4">
                      {rev.isVisible ? (
                        <span className="px-2.5 py-1 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade font-semibold text-[11px]">
                          Hiển Thị
                        </span>
                      ) : (
                        <span className="px-2.5 py-1 rounded-full bg-status-cinnabar/15 text-status-cinnabar font-semibold text-[11px]">
                          Đã Ẩn
                        </span>
                      )}
                    </td>

                    <td className="p-4 text-[11px] text-text-wood-muted">{formatDate(rev.createdAt)}</td>

                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openReplyModal(rev)}
                          className="px-2.5 py-1 rounded-lg bg-primary text-secondary-fixed text-[11px] font-semibold hover:bg-primary-container"
                        >
                          Trả Lời
                        </button>
                        <button
                          onClick={() => handleToggleVisibility(rev.id)}
                          className={`p-1 rounded-lg border text-[11px] ${
                            rev.isVisible
                              ? 'border-status-cinnabar/40 text-status-cinnabar hover:bg-status-cinnabar/10'
                              : 'border-accent-lotus-jade/40 text-accent-lotus-jade hover:bg-accent-lotus-jade/10'
                          }`}
                          title={rev.isVisible ? 'Ẩn đánh giá' : 'Hiện đánh giá'}
                        >
                          <span className="material-symbols-outlined text-[16px]">
                            {rev.isVisible ? 'visibility_off' : 'visibility'}
                          </span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Reply Modal */}
      {replyReviewItem && (
        <div className="fixed inset-0 z-50 bg-bg-agarwood-dark/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface w-full max-w-md rounded-3xl border border-border-bamboo p-6 shadow-2xl space-y-4">
            <h3 className="font-serif text-lg font-bold text-primary">
              Phản Hồi Đánh Giá — {replyReviewItem.userName || `User #${replyReviewItem.userId}`}
            </h3>
            <p className="text-xs text-text-wood-muted bg-bg-warm-sand/50 p-3 rounded-xl border border-border-bamboo">
              “{replyReviewItem.comment || replyReviewItem.content}”
            </p>

            <form onSubmit={handleReplySubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-wood-muted uppercase mb-1">
                  Nội Dung Phản Hồi Từ Mộc Hương *
                </label>
                <textarea
                  rows={4}
                  value={replyMessage}
                  onChange={(e) => setReplyMessage(e.target.value)}
                  placeholder="Kính chào quý khách, Mộc Hương xin tri ân lời nhận xét của bạn..."
                  required
                  className="w-full p-2.5 bg-surface-container-low rounded-xl border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setReplyReviewItem(null)}
                  className="px-4 py-2 text-xs text-text-wood-muted hover:bg-surface-container rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isReplying}
                  className="px-5 py-2.5 bg-primary text-secondary-fixed text-xs font-bold rounded-xl hover:bg-primary-container disabled:opacity-50"
                >
                  {isReplying ? 'Đang lưu...' : 'Gửi Phản Hồi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

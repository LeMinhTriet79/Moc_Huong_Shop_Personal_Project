'use client';

import React, { useEffect, useState, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { identityService } from '@/services/identityService';

function VerifyEmailContent() {
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState<'verifying' | 'success' | 'error'>('verifying');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    async function verify() {
      if (!token) {
        setStatus('error');
        setErrorMessage('Thiếu mã xác thực token');
        return;
      }

      try {
        await identityService.verifyEmail(token);
        setStatus('success');
      } catch (err: any) {
        console.error('Verify email failed:', err);
        setStatus('error');
        setErrorMessage(err.response?.data?.message || 'Mã xác thực đã hết hạn hoặc không tồn tại');
      }
    }
    verify();
  }, [token]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 py-12">
      <div className="max-w-md w-full bg-surface rounded-3xl border border-border-bamboo p-8 text-center space-y-6 shadow-sm">
        {status === 'verifying' && (
          <>
            <div className="w-16 h-16 rounded-full bg-bg-warm-sand flex items-center justify-center text-primary mx-auto border border-border-bamboo animate-spin">
              <span className="material-symbols-outlined text-[32px]">spa</span>
            </div>
            <h1 className="font-serif text-2xl font-bold text-primary">Đang Xác Thực Email...</h1>
            <p className="text-xs text-text-wood-muted">Hệ thống đang kiểm tra thông tin kích hoạt tài khoản của bạn.</p>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="w-16 h-16 rounded-full bg-accent-lotus-jade/15 text-accent-lotus-jade flex items-center justify-center mx-auto border border-accent-lotus-jade/30">
              <span className="material-symbols-outlined text-[36px]">verified</span>
            </div>
            <h1 className="font-serif text-2xl font-bold text-primary">Kích Hoạt Tài Khoản Thành Công!</h1>
            <p className="text-xs text-text-wood-muted leading-relaxed">
              Email của bạn đã được xác minh. Bây giờ bạn có thể trải nghiệm toàn bộ tiện ích của Mộc Hương.
            </p>
            <Link
              href="/login"
              className="inline-block w-full py-3 rounded-xl bg-primary text-secondary-fixed font-label-md font-bold hover:bg-primary-container transition-colors shadow-sm"
            >
              Đăng Nhập Ngay
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <div className="w-16 h-16 rounded-full bg-status-cinnabar/15 text-status-cinnabar flex items-center justify-center mx-auto border border-status-cinnabar/30">
              <span className="material-symbols-outlined text-[36px]">error</span>
            </div>
            <h1 className="font-serif text-2xl font-bold text-primary">Xác Thực Thất Bại</h1>
            <p className="text-xs text-text-wood-muted leading-relaxed">{errorMessage}</p>
            <Link
              href="/login"
              className="inline-block w-full py-3 rounded-xl bg-primary text-secondary-fixed font-label-md font-bold hover:bg-primary-container transition-colors shadow-sm"
            >
              Về Trang Đăng Nhập
            </Link>
          </>
        )}
      </div>
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-background">
          <span className="material-symbols-outlined text-[36px] text-primary animate-spin">spa</span>
        </div>
      }
    >
      <VerifyEmailContent />
    </Suspense>
  );
}

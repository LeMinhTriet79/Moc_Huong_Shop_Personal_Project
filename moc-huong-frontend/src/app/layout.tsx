import type { Metadata } from 'next';
import './globals.css';
import QueryProvider from '@/components/providers/QueryProvider';
import AppShell from '@/components/common/AppShell';

export const metadata: Metadata = {
  title: 'MỘC HƯƠNG — Hương Từ Tâm • Mộc Từ Đất | Trầm Hương & Tinh Dầu Thiên Nhiên',
  description:
    'Không gian thưởng thức Trầm Hương Việt Nam thuần khiết, Nhang thảo mộc tự nhiên, Tinh dầu nguyên chất và Chuỗi hạt thiền định 108 hạt chế tác thủ công.',
  keywords: [
    'Mộc Hương',
    'Trầm hương',
    'Nhang trầm',
    'Trầm nụ',
    'Tinh dầu thiên nhiên',
    'Vòng gỗ phong thủy',
    'Chuỗi 108 hạt',
  ],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body className="bg-background text-text-ink-black antialiased selection:bg-secondary-fixed selection:text-text-ink-black min-h-screen">
        <QueryProvider>
          <AppShell>{children}</AppShell>
        </QueryProvider>
      </body>
    </html>
  );
}

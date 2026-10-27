'use client';

import React, { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';

export default function QueryProvider({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 1000 * 60 * 2, // 2 minutes
            refetchOnWindowFocus: false,
            retry: 1,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3500,
          style: {
            background: '#FAF7F2',
            color: '#231C18',
            border: '1px solid #E5DEC9',
            fontFamily: 'Be Vietnam Pro, sans-serif',
            fontSize: '14px',
            boxShadow: '0 4px 16px rgba(44, 30, 22, 0.08)',
          },
          success: {
            iconTheme: {
              primary: '#3B5E4C',
              secondary: '#FAF7F2',
            },
          },
          error: {
            iconTheme: {
              primary: '#A8382B',
              secondary: '#FAF7F2',
            },
          },
        }}
      />
    </QueryClientProvider>
  );
}

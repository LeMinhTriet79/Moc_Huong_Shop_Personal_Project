'use client';

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { catalogService } from '@/services/catalogService';
import { AiChatResponse, ChatMessageItem, ProductResponse } from '@/types';
import { formatCurrency } from '@/lib/utils';

interface Message {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  suggestedProducts?: ProductResponse[];
  sources?: string[];
  isFallback?: boolean;
}

const QUICK_PROMPTS = [
  'Tư vấn mệnh phong thủy',
  'Cách đốt nụ trầm thác khói',
  'Chính sách freeship & mã giảm giá',
  'Ý nghĩa vòng trầm 108 hạt',
];

export default function AiChatWidget() {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [inputMessage, setInputMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'init-1',
      sender: 'bot',
      text: '🌸 **Kính chào Quý khách!**\n\nMộc Hương là trợ lý AI Hương Đạo thiền tịnh. Quý khách đang muốn tìm kiếm phẩm vật trầm hương để **tĩnh tâm thưởng trà**, **thanh lọc gia đạo** hay cần **tư vấn vòng phong thủy theo Bản Mệnh**?',
    },
  ]);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen, messages]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputMessage).trim();
    if (!text || isLoading) return;

    const userMsg: Message = {
      id: 'user-' + Date.now(),
      sender: 'user',
      text,
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputMessage('');
    setIsLoading(true);

    try {
      // Chuẩn bị history cho Gemini
      const history: ChatMessageItem[] = messages.slice(-6).map((m) => ({
        role: m.sender === 'user' ? 'user' : 'model',
        content: m.text,
      }));

      const res: AiChatResponse = await catalogService.chatWithAi(text, history);

      const botMsg: Message = {
        id: 'bot-' + Date.now(),
        sender: 'bot',
        text: res.reply,
        suggestedProducts: res.suggestedProducts,
        sources: res.sources,
        isFallback: res.isFallback,
      };

      setMessages((prev) => [...prev, botMsg]);
    } catch (err) {
      const errorMsg: Message = {
        id: 'bot-err-' + Date.now(),
        sender: 'bot',
        text: 'Dạ Mộc Hương xin thành thật cáo lỗi, kết nối tới thư viện phong thủy đang tạm gián đoạn. Kính mời Quý khách để lại câu hỏi hoặc thử lại trong giây lát! 🌿',
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="fixed bottom-6 right-6 z-50 select-none">
      {/* Floating Toggle Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="group relative flex items-center gap-2.5 px-4 py-3 rounded-full bg-primary hover:bg-primary-container text-secondary-fixed shadow-2xl transition-all duration-300 hover:scale-105 border border-border-bamboo"
          aria-label="Mở Trợ Lý AI Hương Đạo"
        >
          <span className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-accent-lotus-jade opacity-75" />
            <span className="relative inline-flex rounded-full h-3 w-3 bg-accent-lotus-jade" />
          </span>
          <span className="material-symbols-outlined text-[24px]">spa</span>
          <span className="font-serif font-bold text-xs tracking-wide">Trợ Lý Hương Đạo</span>
        </button>
      )}

      {/* Floating Chat Modal Box */}
      {isOpen && (
        <div className="w-[360px] sm:w-[420px] h-[580px] max-h-[85vh] rounded-3xl bg-surface border border-border-bamboo shadow-2xl flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-5 duration-200">
          {/* Header */}
          <div className="bg-bg-warm-sand/90 p-4 border-b border-border-bamboo flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 border border-primary/20 flex items-center justify-center text-primary shadow-inner">
                <span className="material-symbols-outlined text-[22px]">self_improvement</span>
              </div>
              <div>
                <h3 className="font-serif font-bold text-sm text-primary flex items-center gap-1.5">
                  Trợ Lý Hương Đạo Mộc Hương
                  <span className="w-2 h-2 rounded-full bg-accent-lotus-jade" />
                </h3>
                <p className="text-[11px] text-text-wood-muted flex items-center gap-1">
                  <span>✨ Trực tuyến tĩnh tâm • Tư vấn phong thủy</span>
                </p>
              </div>
            </div>

            <button
              onClick={() => setIsOpen(false)}
              className="w-8 h-8 rounded-xl bg-surface hover:bg-surface-container text-text-wood-muted hover:text-text-ink-black flex items-center justify-center transition-colors border border-border-bamboo"
              title="Đóng cửa sổ"
            >
              <span className="material-symbols-outlined text-[18px]">close</span>
            </button>
          </div>

          {/* Messages Body */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 text-xs font-sans scrollbar-thin">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex flex-col ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}
              >
                <div
                  className={`max-w-[88%] rounded-2xl p-3.5 leading-relaxed shadow-sm ${
                    msg.sender === 'user'
                      ? 'bg-primary text-secondary-fixed rounded-tr-none'
                      : 'bg-surface-container-low border border-border-bamboo text-text-ink-black rounded-tl-none whitespace-pre-line'
                  }`}
                >
                  {msg.text}
                </div>

                {/* Suggested Products Carousel/Cards */}
                {msg.suggestedProducts && msg.suggestedProducts.length > 0 && (
                  <div className="mt-2.5 space-y-2 w-full max-w-[92%]">
                    <span className="text-[11px] font-bold text-primary flex items-center gap-1">
                      <span className="material-symbols-outlined text-[14px]">local_mall</span>
                      Sản phẩm tương hợp gợi ý:
                    </span>
                    <div className="grid grid-cols-1 gap-2">
                      {msg.suggestedProducts.map((p) => (
                        <Link
                          key={p.id}
                          href={`/products/${p.id}`}
                          onClick={() => setIsOpen(false)}
                          className="p-2.5 rounded-xl bg-surface hover:bg-bg-warm-sand/80 border border-border-bamboo flex items-center justify-between gap-3 transition-colors group"
                        >
                          <div className="flex items-center gap-2.5 overflow-hidden">
                            <div className="w-10 h-10 rounded-lg bg-bg-warm-sand border border-border-bamboo shrink-0 overflow-hidden">
                              {/* eslint-disable-next-line @next/next/no-img-element */}
                              <img
                                src={p.primaryImageUrl || p.images?.[0]?.imageUrl || '/images/placeholder.jpg'}
                                alt={p.name}
                                className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                              />
                            </div>
                            <div className="truncate">
                              <h5 className="font-serif font-bold text-[12px] text-text-ink-black truncate group-hover:text-primary transition-colors">
                                {p.name}
                              </h5>
                              <p className="text-[10px] text-text-wood-muted font-mono">
                                {p.basePrice ? formatCurrency(p.basePrice) : 'Liên hệ'} • ⭐ {p.averageRating || 5.0}
                              </p>
                            </div>
                          </div>
                          <span className="material-symbols-outlined text-text-wood-muted text-[16px] group-hover:translate-x-0.5 transition-transform">
                            chevron_right
                          </span>
                        </Link>
                      ))}
                    </div>
                  </div>
                )}

                {/* Sources & Fallback badge */}
                {msg.sources && msg.sources.length > 0 && (
                  <div className="mt-1 flex items-center gap-1.5 text-[10px] text-text-wood-muted px-1">
                    <span className="material-symbols-outlined text-[12px]">menu_book</span>
                    <span>Nguồn: {msg.sources.join(' • ')}</span>
                  </div>
                )}
              </div>
            ))}

            {isLoading && (
              <div className="flex items-center gap-2 text-xs text-text-wood-muted italic bg-surface-container-low/60 p-3 rounded-2xl border border-border-bamboo max-w-[70%]">
                <span className="w-2 h-2 rounded-full bg-accent-lotus-jade animate-ping" />
                <span>Trợ lý đang tĩnh tâm suy ngẫm...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick Prompts */}
          <div className="px-3 py-2 bg-bg-warm-sand/50 border-t border-border-bamboo flex items-center gap-1.5 overflow-x-auto scrollbar-none text-[11px]">
            {QUICK_PROMPTS.map((prompt, idx) => (
              <button
                key={idx}
                onClick={() => handleSendMessage(prompt)}
                disabled={isLoading}
                className="px-2.5 py-1 rounded-full bg-surface hover:bg-surface-container border border-border-bamboo text-text-wood-muted hover:text-primary whitespace-nowrap transition-colors shrink-0 disabled:opacity-50"
              >
                {prompt}
              </button>
            ))}
          </div>

          {/* Input Footer */}
          <div className="p-3 bg-surface border-t border-border-bamboo flex items-center gap-2">
            <input
              ref={inputRef}
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Hỏi về phong thủy, nụ trầm, freeship..."
              className="flex-1 px-3.5 py-2.5 rounded-2xl bg-surface-container-low border border-border-bamboo text-xs text-text-ink-black focus:outline-none focus:border-primary placeholder:text-text-wood-muted/70"
            />
            <button
              onClick={() => handleSendMessage()}
              disabled={isLoading || !inputMessage.trim()}
              className="w-10 h-10 rounded-2xl bg-primary text-secondary-fixed hover:bg-primary-container disabled:opacity-40 flex items-center justify-center transition-all shadow-md shrink-0"
              title="Gửi câu hỏi"
            >
              <span className="material-symbols-outlined text-[18px]">send</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

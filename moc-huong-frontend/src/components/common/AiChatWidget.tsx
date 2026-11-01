'use client';

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { catalogService } from '@/services/catalogService';
import { AiChatResponse, ChatMessageItem, ProductResponse } from '@/types';
import { formatCurrency } from '@/lib/utils';
import { useCartStore } from '@/store/useCartStore';
import RichTextContent from '@/components/common/RichTextContent';

interface Message {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  suggestedProducts?: ProductResponse[];
  sources?: string[];
  isFallback?: boolean;
}

const QUICK_PROMPTS = [
  'Tư vấn mệnh Hỏa & Thổ',
  'Tư vấn mệnh Kim & Thủy',
  'Cách đốt nụ trầm thác khói',
  'Chính sách freeship & mã giảm giá',
];

export default function AiChatWidget() {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [inputMessage, setInputMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { addItem } = useCartStore();

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

  const handleQuickAddToCart = async (e: React.MouseEvent, p: ProductResponse) => {
    e.preventDefault();
    e.stopPropagation();
    let variant = p.variants?.[0];
    if (!variant) {
      try {
        const detail = await catalogService.getProductById(p.slug || p.id);
        variant = detail?.variants?.[0];
      } catch {
        // ignore
      }
    }
    const imgUrl = p.primaryImageUrl || p.images?.[0]?.imageUrl || 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=400';
    await addItem({
      productId: p.id,
      variantId: variant?.id || p.id,
      sku: variant?.sku || p.skuPrefix || `MH-${p.id}`,
      productName: p.name,
      variantName: variant?.variantName || 'Tiêu chuẩn',
      productImageUrl: imgUrl,
      price: variant?.price || p.basePrice || 350000,
      quantity: 1,
    });
  };

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
    } catch {
      const errorMsg: Message = {
        id: 'bot-err-' + Date.now(),
        sender: 'bot',
        text: 'Dạ Mộc Hương xin thành thật cáo lỗi, kết nối tới thư viện phong thủy đang tạm gián đoạn. Kính mời Quý khách thử lại trong giây lát! 🌿',
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
          <span className="material-symbols-outlined text-[22px]">spa</span>
          <span className="font-serif font-bold text-xs tracking-wide">Trợ Lý Hương Đạo</span>
        </button>
      )}

      {isOpen && (
        <div className="w-[350px] sm:w-[410px] h-[580px] max-h-[85vh] rounded-3xl bg-surface border border-border-bamboo shadow-2xl flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-5 duration-200">
          {/* Header */}
          <div className="bg-bg-agarwood-dark text-surface-bright p-4 border-b border-border-bamboo/30 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-secondary-fixed/20 border border-secondary-fixed/30 flex items-center justify-center text-secondary-fixed shadow-inner">
                <span className="material-symbols-outlined text-[22px]">self_improvement</span>
              </div>
              <div>
                <h3 className="font-serif font-bold text-sm text-surface-bright flex items-center gap-1.5">
                  Trợ Lý Hương Đạo Mộc Hương
                  <span className="w-2 h-2 rounded-full bg-accent-lotus-jade" />
                </h3>
                <p className="text-[11px] text-secondary-fixed/90 flex items-center gap-1">
                  <span>✨ Cố vấn Phong Thủy & Trầm Đạo AI</span>
                </p>
              </div>
            </div>

            <button
              onClick={() => setIsOpen(false)}
              className="w-8 h-8 rounded-xl bg-surface-container-highest/15 hover:bg-surface-container-highest/30 text-surface-bright flex items-center justify-center transition-colors"
              title="Đóng cửa sổ"
            >
              <span className="material-symbols-outlined text-[18px]">close</span>
            </button>
          </div>

          {/* Messages Body */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 text-xs font-sans">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex flex-col ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}
              >
                <div
                  className={`max-w-[90%] rounded-2xl p-3.5 leading-relaxed shadow-sm ${
                    msg.sender === 'user'
                      ? 'bg-primary text-secondary-fixed rounded-tr-none'
                      : 'bg-surface-container-low border border-border-bamboo text-text-ink-black rounded-tl-none'
                  }`}
                >
                  {msg.sender === 'user' ? msg.text : <RichTextContent content={msg.text} />}
                </div>

                {/* Suggested Products Cards */}
                {msg.suggestedProducts && msg.suggestedProducts.length > 0 && (
                  <div className="mt-2.5 space-y-2 w-full max-w-[94%]">
                    <span className="text-[11px] font-bold text-primary flex items-center gap-1">
                      <span className="material-symbols-outlined text-[14px]">local_mall</span>
                      Phẩm vật tương hợp gợi ý:
                    </span>
                    <div className="grid grid-cols-1 gap-2">
                      {msg.suggestedProducts.map((p) => (
                        <div
                          key={p.id}
                          className="p-2.5 rounded-xl bg-bg-warm-sand/60 hover:bg-bg-warm-sand border border-border-bamboo flex items-center justify-between gap-2 transition-colors"
                        >
                          <Link
                            href={`/products/${p.id}`}
                            onClick={() => setIsOpen(false)}
                            className="flex items-center gap-2.5 overflow-hidden flex-1 group"
                          >
                            <div className="w-11 h-11 rounded-lg bg-surface border border-border-bamboo shrink-0 overflow-hidden">
                              {/* eslint-disable-next-line @next/next/no-img-element */}
                              <img
                                src={p.primaryImageUrl || p.images?.[0]?.imageUrl || 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=200'}
                                alt={p.name}
                                className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                              />
                            </div>
                            <div className="truncate">
                              <h5 className="font-serif font-bold text-[12px] text-text-ink-black truncate group-hover:text-primary transition-colors">
                                {p.name}
                              </h5>
                              <p className="text-[11px] text-secondary font-mono font-bold">
                                {p.basePrice ? formatCurrency(p.basePrice) : 'Liên hệ'}
                              </p>
                            </div>
                          </Link>

                          <button
                            type="button"
                            onClick={(e) => handleQuickAddToCart(e, p)}
                            className="px-2.5 py-1.5 rounded-lg bg-primary text-secondary-fixed hover:bg-primary-container text-[11px] font-semibold shrink-0 flex items-center gap-1 shadow-xs"
                            title="Thêm nhanh vào giỏ hàng"
                          >
                            <span className="material-symbols-outlined text-[14px]">add_shopping_cart</span>
                            <span>Chọn</span>
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {msg.sources && msg.sources.length > 0 && (
                  <div className="mt-1 flex items-center gap-1 text-[10px] text-text-wood-muted px-1">
                    <span className="material-symbols-outlined text-[12px]">menu_book</span>
                    <span>Nguồn: {msg.sources.join(' • ')}</span>
                  </div>
                )}
              </div>
            ))}

            {isLoading && (
              <div className="flex items-center gap-2 text-xs text-text-wood-muted italic bg-surface-container-low/60 p-3 rounded-2xl border border-border-bamboo max-w-[75%]">
                <span className="w-2 h-2 rounded-full bg-accent-lotus-jade animate-ping" />
                <span>Trợ lý đang tĩnh tâm suy ngẫm...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick Prompts */}
          <div className="px-3 py-2 bg-bg-warm-sand/60 border-t border-border-bamboo flex items-center gap-1.5 overflow-x-auto scrollbar-none text-[11px]">
            {QUICK_PROMPTS.map((prompt, idx) => (
              <button
                key={idx}
                onClick={() => handleSendMessage(prompt)}
                disabled={isLoading}
                className="px-2.5 py-1 rounded-full bg-surface hover:bg-primary hover:text-secondary-fixed border border-border-bamboo text-text-wood-muted whitespace-nowrap transition-colors shrink-0 disabled:opacity-50"
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
              placeholder="Hỏi về mệnh phong thủy, nụ trầm, freeship..."
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

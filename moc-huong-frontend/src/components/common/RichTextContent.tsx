'use client';

import React from 'react';

interface RichTextContentProps {
  content?: string | null;
  className?: string;
}

export function stripHtmlTags(raw?: string | null): string {
  if (!raw) return '';
  return raw
    .replace(/<\/p>/gi, '\n\n')
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .trim();
}

export default function RichTextContent({ content, className = '' }: RichTextContentProps) {
  if (!content) return null;

  const cleaned = stripHtmlTags(content);
  const lines = cleaned.split('\n');

  const renderInlineBold = (text: string) => {
    const parts = text.split(/(\*\*[^*]+\*\*|`[^`]+`)/g);
    return parts.map((part, idx) => {
      if (part.startsWith('**') && part.endsWith('**')) {
        return (
          <strong key={idx} className="font-bold text-primary">
            {part.slice(2, -2)}
          </strong>
        );
      }
      if (part.startsWith('`') && part.endsWith('`')) {
        return (
          <code
            key={idx}
            className="font-mono text-[11px] px-1.5 py-0.5 rounded bg-secondary-fixed/40 text-primary font-bold"
          >
            {part.slice(1, -1)}
          </code>
        );
      }
      return <React.Fragment key={idx}>{part}</React.Fragment>;
    });
  };

  return (
    <div className={`space-y-2 ${className}`}>
      {lines.map((rawLine, idx) => {
        const line = rawLine.trim();
        if (!line) return <div key={idx} className="h-1" />;

        if (line.startsWith('#### ')) {
          return (
            <h4 key={idx} className="font-serif font-bold text-sm text-primary pt-1">
              {renderInlineBold(line.slice(5))}
            </h4>
          );
        }
        if (line.startsWith('### ')) {
          return (
            <h3 key={idx} className="font-serif font-bold text-base text-primary pt-1">
              {renderInlineBold(line.slice(4))}
            </h3>
          );
        }
        if (line.startsWith('- ') || line.startsWith('* ')) {
          return (
            <div key={idx} className="flex items-start gap-2 pl-1">
              <span className="text-secondary mt-0.5">•</span>
              <span className="flex-1">{renderInlineBold(line.slice(2))}</span>
            </div>
          );
        }
        return (
          <p key={idx} className="leading-relaxed">
            {renderInlineBold(line)}
          </p>
        );
      })}
    </div>
  );
}

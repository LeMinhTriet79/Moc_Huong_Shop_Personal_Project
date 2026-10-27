import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Flame, Sparkles, Feather, MapPin, Phone, Mail, Clock, ArrowRight } from 'lucide-react';

export const metadata = {
  title: 'Câu Chuyện Mộc Hương — Tinh Hoa Trầm Hương Thuần Khiết Đông Phương',
  description: 'Hành trình gìn giữ và kế thừa tinh hoa nghệ thuật làm trầm hương truyền thống xứ Quảng Nam, mang lại nguồn năng lượng an định, tĩnh tâm cho không gian sống hiện đại.',
};

export default function AboutPage() {
  return (
    <div className="bg-[#FAF7F2] min-h-screen text-[#1E1815]">
      {/* Hero Section */}
      <section className="relative py-24 md:py-32 overflow-hidden bg-gradient-to-b from-[#F2ECE1] to-[#FAF7F2] border-b border-[#E5DEC9]">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 text-center relative z-10">
          <span className="inline-flex items-center gap-2 px-3 py-1 bg-[#FAF7F2] border border-[#C89B57]/40 rounded-full text-xs font-semibold uppercase tracking-widest text-[#C89B57] mb-6">
            <Sparkles className="w-3.5 h-3.5" /> Khởi Nguyên Mộc Hương
          </span>
          <h1 className="font-serif text-4xl sm:text-5xl md:text-6xl text-[#1E1815] leading-tight mb-8">
            Hương Khói Tĩnh Lặng <br />
            <span className="italic text-[#C89B57] font-normal">Chạm Đến An Nhiên Nội Tại</span>
          </h1>
          <p className="max-w-2xl mx-auto text-base sm:text-lg text-[#1E1815]/75 font-sans leading-relaxed">
            Từ đại ngàn Tiên Phước - Quảng Nam, nơi hội tụ linh khí của đất trời qua hàng chục năm trong lòng cây Dó Bầu, Mộc Hương nâng niu từng dải khói thanh tao để trao gửi bình yên đến tổ ấm của bạn.
          </p>
        </div>

        {/* Subtle decorative smoke blur */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-[#C89B57]/5 rounded-full blur-3xl pointer-events-none" />
      </section>

      {/* Brand Philosophy */}
      <section className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div className="relative aspect-[4/5] rounded-2xl overflow-hidden shadow-xl border border-[#E5DEC9] bg-[#F2ECE1]">
            <Image
              src="https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?q=80&w=1200"
              alt="Nghệ thuật chế tác trầm hương thủ công"
              fill
              className="object-cover"
              sizes="(max-width: 1024px) 100vw, 50vw"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-[#1E1815]/60 via-transparent to-transparent" />
            <div className="absolute bottom-6 left-6 right-6 p-6 bg-[#FAF7F2]/90 backdrop-blur-md rounded-xl border border-[#C89B57]/30">
              <p className="font-serif italic text-sm text-[#1E1815]/90">
                &ldquo;Trầm hương không đơn thuần là một mùi hương, đó là sự tích lũy của vết thương lành lặn qua năm tháng, hóa thành bảo vật linh thiêng của thiên nhiên.&rdquo;
              </p>
              <p className="text-xs uppercase tracking-wider text-[#C89B57] font-medium mt-2">
                — Nghệ Nhân Nguyễn Hữu Trí (Cố vấn Mộc Hương)
              </p>
            </div>
          </div>

          <div className="space-y-8">
            <div>
              <span className="text-xs uppercase font-semibold tracking-widest text-[#3B5E4C]">Triết Lý Chế Tác</span>
              <h2 className="font-serif text-3xl sm:text-4xl text-[#1E1815] mt-2 mb-6 leading-snug">
                Thuần Khiết Tự Nhiên, Không Pha Tạp Hóa Chất
              </h2>
              <p className="text-sm sm:text-base text-[#1E1815]/75 leading-relaxed font-sans mb-4">
                Giữa thời đại công nghiệp hóa với vô vàn hương liệu nhân tạo và chất dẫn cháy độc hại, Mộc Hương kiên định đi theo con đường truyền thống: 100% bột trầm kiến tự nhiên kết hợp với keo vỏ cây bời lời thuần túy.
              </p>
              <p className="text-sm sm:text-base text-[#1E1815]/75 leading-relaxed font-sans">
                Khói trầm Mộc Hương trắng mảnh, uốn lượn thong dong, không gây cay mắt, không khét nồng, giữ nguyên tầng hương ngọt dịu thảo mộc sâu lắng, thích hợp cho phòng thiền trà, bàn thờ gia tiên và không gian tĩnh dưỡng.
              </p>
            </div>

            <div className="grid grid-cols-2 gap-6 pt-4 border-t border-[#E5DEC9]">
              <div className="flex gap-4">
                <div className="w-10 h-10 rounded-full bg-[#3B5E4C]/10 text-[#3B5E4C] flex items-center justify-center shrink-0">
                  <Feather className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-serif font-bold text-base text-[#1E1815]">100% Thuần Khiết</h3>
                  <p className="text-xs text-[#1E1815]/70 mt-1">Không hương liệu tổng hợp, an toàn tuyệt đối cho người già và trẻ nhỏ.</p>
                </div>
              </div>

              <div className="flex gap-4">
                <div className="w-10 h-10 rounded-full bg-[#C89B57]/10 text-[#C89B57] flex items-center justify-center shrink-0">
                  <Flame className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-serif font-bold text-base text-[#1E1815]">Ủ Mộc Tự Nhiên</h3>
                  <p className="text-xs text-[#1E1815]/70 mt-1">Ủ thảo mộc tự nhiên trên 180 ngày để mùi hăng dịu đi, đọng lại vị ngọt thanh khiết.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Crafting Process Steps */}
      <section className="py-20 bg-[#F2ECE1] border-y border-[#E5DEC9]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs uppercase font-semibold tracking-widest text-[#C89B57]">Quy Trình Chế Tác</span>
            <h2 className="font-serif text-3xl sm:text-4xl text-[#1E1815] mt-2 mb-4">Bốn Bước Khởi Duyên Hương</h2>
            <p className="text-sm sm:text-base text-[#1E1815]/70">
              Từng nén nhang, từng mảnh trầm nụ đều trải qua quá trình tuyển chọn và đúc nắn khắt khe bởi bàn tay nghệ nhân bản xứ.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {[
              {
                step: '01',
                title: 'Tuyển Chọn Gỗ Dó',
                desc: 'Chọn lọc những mảnh giác trầm tích tụ trên 15 năm từ những thân cây Dó Bầu già cỗi tại núi rừng Tiên Phước.',
              },
              {
                step: '02',
                title: 'Xay Nghiền & Tinh Lọc',
                desc: 'Gỗ trầm được tỉa sạch tạp chất, sau đó xay mịn bằng cối đá thủ công để giữ trọn vẹn tinh dầu trầm quý giá.',
              },
              {
                step: '03',
                title: 'Hòa Keo Bời Lời Tự Nhiên',
                desc: 'Phối trộn bột trầm với vỏ cây bời lời khô và nước thanh khiết theo tỷ lệ vàng gia truyền, tuyệt đối không keo hóa học.',
              },
              {
                step: '04',
                title: 'Phơi Gió Trong Bóng Râm',
                desc: 'Hong khô hoàn toàn bằng gió tự nhiên trong phòng sấy thoáng mát, tránh ánh nắng gắt để hương không bị bay hơi.',
              },
            ].map((item, idx) => (
              <div key={idx} className="bg-[#FAF7F2] p-8 rounded-2xl border border-[#E5DEC9] relative group hover:shadow-lg transition-all duration-300">
                <span className="font-serif text-4xl text-[#C89B57]/30 group-hover:text-[#C89B57] transition-colors block mb-4 font-bold">
                  {item.step}
                </span>
                <h3 className="font-serif text-lg font-bold text-[#1E1815] mb-2">{item.title}</h3>
                <p className="text-xs sm:text-sm text-[#1E1815]/75 leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Showroom Experience */}
      <section className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-[#1E1815] text-[#FAF7F2] rounded-3xl p-8 sm:p-12 lg:p-16 relative overflow-hidden">
          <div className="relative z-10 max-w-2xl">
            <span className="text-xs uppercase font-semibold tracking-widest text-[#C89B57] mb-2 block">
              Không Gian Thưởng Trầm
            </span>
            <h2 className="font-serif text-3xl sm:text-4xl text-[#FAF7F2] mb-6 leading-tight">
              Trải Nghiệm Trực Tiếp Tại Showroom Mộc Hương
            </h2>
            <p className="text-sm sm:text-base text-[#FAF7F2]/80 leading-relaxed mb-8">
              Kính mời quý bạn hữu ghé thăm không gian Trà Trầm của Mộc Hương để thưởng trà, thưởng hương và tự tay chọn cho mình mùi hương tâm đắc nhất.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs sm:text-sm text-[#FAF7F2]/90 mb-8">
              <div className="flex items-center gap-3">
                <MapPin className="w-4 h-4 text-[#C89B57] shrink-0" />
                <span>128 Nguyễn Đình Chiểu, Phường Đa Kao, Quận 1, TP. Hồ Chí Minh</span>
              </div>
              <div className="flex items-center gap-3">
                <Clock className="w-4 h-4 text-[#C89B57] shrink-0" />
                <span>08:30 – 21:00 (Mỗi ngày kể cả Lễ, Tết)</span>
              </div>
              <div className="flex items-center gap-3">
                <Phone className="w-4 h-4 text-[#C89B57] shrink-0" />
                <span>Hotline: 0903 123 456 (Đặt hẹn thưởng trà)</span>
              </div>
              <div className="flex items-center gap-3">
                <Mail className="w-4 h-4 text-[#C89B57] shrink-0" />
                <span>lienhe@mochuong.vn</span>
              </div>
            </div>

            <Link
              href="/shop"
              className="inline-flex items-center gap-2 px-8 py-3.5 bg-[#C89B57] hover:bg-[#B38746] text-[#FAF7F2] font-serif font-medium rounded-xl transition-all shadow-md group"
            >
              <span>Khám Phá Bộ Sưu Tập</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Link>
          </div>

          <div className="absolute right-0 bottom-0 top-0 w-1/3 opacity-20 lg:opacity-30 pointer-events-none hidden md:block">
            <Image
              src="https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=1200"
              alt="Thiền trà Mộc Hương"
              fill
              className="object-cover"
            />
          </div>
        </div>
      </section>
    </div>
  );
}

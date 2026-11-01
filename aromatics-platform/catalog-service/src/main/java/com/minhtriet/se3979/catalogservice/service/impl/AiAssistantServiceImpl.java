package com.minhtriet.se3979.catalogservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.AiChatRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiGenerateDescriptionRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiSuggestReplyRequest;
import com.minhtriet.se3979.catalogservice.dto.response.AiChatResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import com.minhtriet.se3979.catalogservice.entity.Product;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.service.AiAssistantService;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.url:https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}}")
    private String urlTemplate;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String userMsg = request.getMessage() != null ? request.getMessage().trim() : "";

        // 1. RAG Retrieval: Tìm kiếm sản phẩm liên quan từ Database thực tế
        List<ProductResponse> suggestedProducts = findRelevantProducts(userMsg);

        // 2. Chuẩn bị Context sản phẩm thực tế
        StringBuilder catalogContext = new StringBuilder();
        if (!suggestedProducts.isEmpty()) {
            catalogContext.append("\n[DANH MỤC SẢN PHẨM MỘC HƯƠNG HIỆN CÓ SẴN]:\n");
            for (ProductResponse p : suggestedProducts) {
                catalogContext.append(String.format("- %s (Mã #%d, Giá từ: %s ₫, Đánh giá: %.1f★): %s\n",
                        p.getName(), p.getId(),
                        p.getBasePrice() != null ? p.getBasePrice().toPlainString() : "Liên hệ",
                        p.getAverageRating() != null ? p.getAverageRating().doubleValue() : 5.0,
                        p.getShortDescription() != null ? p.getShortDescription() : "Hương trầm tự nhiên thanh tịnh"));
            }
        }

        // 3. Xây dựng System Prompt cho Gemini
        String systemInstruction = "Bạn là Cố Vấn Tĩnh Tâm & Trợ Lý Phong Thủy Am Trầm Hương Mộc Hương (Mộc Hương Shop).\n" +
                "Tôn chỉ của bạn: Luôn dùng ngôn từ thanh nhã, ấm áp, đậm chất Thiền Định Đông Phương (gọi khách hàng là 'Quý khách' hoặc 'Đồng đạo', xưng là 'Mộc Hương').\n" +
                "Bạn có kiến thức sâu rộng về Ngũ Hành Phong Thủy (Kim, Mộc, Thủy, Hỏa, Thổ), trầm hương tự nhiên, chuỗi 108 hạt, nụ trầm thác khói, nhang không tăm, tinh dầu xông.\n" +
                "Hãy kết hợp tư vấn phong thủy và giới thiệu khéo léo các sản phẩm Mộc Hương dưới đây:\n" +
                catalogContext + "\n" +
                "Chính sách cửa hàng: Miễn phí vận chuyển toàn quốc cho đơn từ 300.000 ₫ (Mã FREESHIP0D), Mã giảm giá TINHTAM10 hoặc MOCHUONG10 giảm 10% đơn hàng.\n" +
                "Định dạng câu trả lời dùng Markdown đẹp mắt (gạch đầu dòng, in đậm tên sản phẩm).";

        // 4. Thử gọi Google Gemini API
        try {
            String fullPrompt = systemInstruction + "\n\nCâu hỏi của Quý khách: " + userMsg;
            String geminiReply = callGeminiApi(fullPrompt, request.getHistory());
            if (geminiReply != null && !geminiReply.isBlank()) {
                return AiChatResponse.builder()
                        .reply(geminiReply)
                        .suggestedProducts(suggestedProducts)
                        .sources(List.of("Mộc Hương Trầm Thư", "Ngũ Hành Bát Quái Đông Phương", "Cơ sở dữ liệu Mộc Hương Shop"))
                        .isFallback(false)
                        .build();
            }
        } catch (Exception ex) {
            log.warn("Gemini API call failed ({}), activating Smart Domain Fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
        }

        // 5. Fallback Thông Minh (Smart Domain Fallback RAG Engine)
        String fallbackReply = generateDomainFallbackReply(userMsg, suggestedProducts);
        return AiChatResponse.builder()
                .reply(fallbackReply)
                .suggestedProducts(suggestedProducts)
                .sources(List.of("Mộc Hương Trầm Thư (Nội bộ)", "Cẩm Nang Thưởng Trầm Thiền Tịnh"))
                .isFallback(true)
                .build();
    }

    @Override
    public AiChatResponse generateProductDescription(AiGenerateDescriptionRequest request) {
        String prompt = String.format(
                "Bạn là chuyên gia Content Marketing phong thủy cao cấp của Mộc Hương Shop.\n" +
                "Hãy viết bài mô tả sản phẩm thương mại điện tử thật cuốn hút, đậm chất thiền định, chuẩn SEO cho sản phẩm sau:\n" +
                "- Tên sản phẩm: %s\n" +
                "- Danh mục: %s\n" +
                "- Từ khóa phong thủy/hương liệu: %s\n" +
                "- Đối tượng khách hàng: %s\n\n" +
                "Yêu cầu cấu trúc bài viết (Markdown):\n" +
                "1. Lời mở đầu đầy thiền vị chạm đến tâm thức\n" +
                "2. Tầng hương đặc trưng (Hương đầu - Hương giữa - Hương cuối)\n" +
                "3. Ý nghĩa Phong thủy & Ngũ hành tương sinh\n" +
                "4. Hướng dẫn sử dụng & Bảo quản mộc trầm thanh khiết\n" +
                "5. Cam kết chất lượng từ Mộc Hương Shop",
                request.getProductName(),
                request.getCategoryName() != null ? request.getCategoryName() : "Trầm Hương Cao Cấp",
                request.getKeywords() != null ? String.join(", ", request.getKeywords()) : "Trầm hương, an thần, thanh tịnh",
                request.getTargetAudience() != null ? request.getTargetAudience() : "Thiền định, an cư, quà tặng cao cấp"
        );

        try {
            String geminiReply = callGeminiApi(prompt, null);
            if (geminiReply != null && !geminiReply.isBlank()) {
                return AiChatResponse.builder()
                        .reply(geminiReply)
                        .sources(List.of("Google Gemini AI Model", "Tiêu chuẩn Content Mộc Hương"))
                        .isFallback(false)
                        .build();
            }
        } catch (Exception ex) {
            log.warn("Gemini generateDescription error: {}", ex.getMessage());
        }

        // Fallback Description
        String fallbackDesc = String.format(
                "### 🌿 %s – Tinh Hoa Đất Trời, An Nhiên Tĩnh Lạc\n\n" +
                "Giữa nhịp sống hối hả, **%s** mang đến cho không gian của Quý khách sự an yên, thuần khiết từ mộc trầm rừng già tích tụ linh khí ngàn năm.\n\n" +
                "#### 🌸 Các Tầng Hương Thanh Nhã:\n" +
                "- **Hương đầu:** Thoang thoảng dịu ngọt, gạt bỏ âu lo thường nhật.\n" +
                "- **Hương giữa:** Trầm ấm, sâu lắng, giúp định tâm khi tọa thiền hoặc thưởng trà.\n" +
                "- **Hương cuối:** Lưu luyến bền bỉ, thanh lọc không gian và thu hút sinh khí cát tường.\n\n" +
                "#### ☸ Ý Nghĩa Phong Thủy:\n" +
                "Trầm hương là vật phẩm mộc thuần khiết, mang tính dương cực thịnh, giúp hóa giải uế khí, trợ mệnh gia chủ bình an, gia đạo hưng thịnh và tâm an vạn sự lành.\n\n" +
                "#### 📦 Cam Kết Từ Mộc Hương Shop:\n" +
                "- 100%% trầm hương tự nhiên không hóa chất độc hại.\n" +
                "- Đổi trả linh hoạt trong 7 ngày nếu không hài lòng.",
                request.getProductName(), request.getProductName()
        );

        return AiChatResponse.builder()
                .reply(fallbackDesc)
                .sources(List.of("Mẫu Mô Tả Phong Thủy Mộc Hương (Nội bộ)"))
                .isFallback(true)
                .build();
    }

    @Override
    public AiChatResponse suggestReviewReply(AiSuggestReplyRequest request) {
        String prompt = String.format(
                "Bạn là Quản lý Chăm Sóc Khách Hàng của Mộc Hương Shop.\n" +
                "Hãy soạn một lời phản hồi thật chân thành, lịch thiệp, đậm chất văn hóa hiếu khách Đông Phương cho đánh giá của khách hàng:\n" +
                "- Tên khách hàng: %s\n" +
                "- Sản phẩm: %s\n" +
                "- Số sao đánh giá: %d / 5 sao\n" +
                "- Nội dung nhận xét: \"%s\"\n\n" +
                "Nếu khách hài lòng (4-5 sao): Bày tỏ lòng tri ân sâu sắc, chúc gia đạo bình an, tâm thân hoan hỷ.\n" +
                "Nếu khách chưa hài lòng (1-3 sao): Tạ lỗi nhã nhặn, cam kết giải quyết thấu đáo và cung cấp hotline hỗ trợ bảo hành tận tình.",
                request.getCustomerName() != null ? request.getCustomerName() : "Quý khách",
                request.getProductName() != null ? request.getProductName() : "Sản phẩm",
                request.getRating() != null ? request.getRating() : 5,
                request.getComment()
        );

        try {
            String geminiReply = callGeminiApi(prompt, null);
            if (geminiReply != null && !geminiReply.isBlank()) {
                return AiChatResponse.builder()
                        .reply(geminiReply)
                        .sources(List.of("Google Gemini AI Model", "Quy Chuẩn Phục Vụ Mộc Hương"))
                        .isFallback(false)
                        .build();
            }
        } catch (Exception ex) {
            log.warn("Gemini suggestReviewReply error: {}", ex.getMessage());
        }

        // Fallback Reply
        int rating = request.getRating() != null ? request.getRating() : 5;
        String name = request.getCustomerName() != null ? request.getCustomerName() : "Quý khách";
        String fallbackReply;
        if (rating >= 4) {
            fallbackReply = String.format(
                    "Dạ Mộc Hương xin thành kính tri ân %s đã gửi trọn niềm tin và chia sẻ cảm nhận tuyệt vời này! " +
                    "Nguyện chúc hương trầm thanh tịnh luôn mang lại an lạc, cát tường và thịnh vượng đến không gian sống của %s và gia quyến. " +
                    "Mộc Hương luôn sẵn lòng đồng hành cùng Quý khách trên hành trình thưởng trầm tĩnh tâm. 🌸",
                    name, name
            );
        } else {
            fallbackReply = String.format(
                    "Dạ Mộc Hương chân thành cảm ơn phản hồi đóng góp quý báu từ %s và thành thật cáo lỗi vì trải nghiệm chưa được trọn vẹn lần này. " +
                    "Mộc Hương luôn đặt sự hài lòng của Quý khách lên hàng đầu. Đội ngũ chuyên viên Mộc Hương xin phép liên hệ hỗ trợ đổi mới hoặc chăm sóc sản phẩm tận tình ngay hôm nay qua số điện thoại của Quý khách. Kính chúc %s vạn sự an khang!",
                    name, name
            );
        }

        return AiChatResponse.builder()
                .reply(fallbackReply)
                .sources(List.of("Mẫu Phản Hồi Chăm Sóc Khách Hàng Mộc Hương"))
                .isFallback(true)
                .build();
    }

    // ==========================================
    // HELPER: Gọi REST API Google Gemini
    // ==========================================
    private String callGeminiApi(String prompt, List<AiChatRequest.ChatMessageItem> history) throws Exception {
        String endpoint = urlTemplate.replace("{model}", model).replace("{apiKey}", apiKey);

        List<Map<String, Object>> contents = new ArrayList<>();

        // Nạp history nếu có
        if (history != null && !history.isEmpty()) {
            for (AiChatRequest.ChatMessageItem item : history) {
                String role = "model".equalsIgnoreCase(item.getRole()) ? "model" : "user";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", item.getContent()))
                ));
            }
        }

        // Tin nhắn hiện tại
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
        ));

        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1200
                )
        );

        RestClient restClient = RestClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        String rawResponse = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(requestBody))
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                return parts.get(0).path("text").asText();
            }
        }
        return null;
    }

    // ==========================================
    // HELPER: RAG Tìm Sản Phẩm Thực Tế Từ DB
    // ==========================================
    private List<ProductResponse> findRelevantProducts(String message) {
        String lower = message.toLowerCase();
        try {
            if (lower.contains("vòng") || lower.contains("chuỗi") || lower.contains("đeo tay")) {
                var page = productService.searchProducts("vòng", null, null, null, null, PageRequest.of(0, 3));
                if (page.hasContent()) return page.getContent();
            }
            if (lower.contains("nụ") || lower.contains("thác khói")) {
                var page = productService.searchProducts("nụ", null, null, null, null, PageRequest.of(0, 3));
                if (page.hasContent()) return page.getContent();
            }
            if (lower.contains("nhang") || lower.contains("tăm")) {
                var page = productService.searchProducts("nhang", null, null, null, null, PageRequest.of(0, 3));
                if (page.hasContent()) return page.getContent();
            }
            if (lower.contains("tinh dầu") || lower.contains("xông")) {
                var page = productService.searchProducts("tinh dầu", null, null, null, null, PageRequest.of(0, 3));
                if (page.hasContent()) return page.getContent();
            }
            // Fallback lấy Top Featured
            return productService.getFeaturedProducts().stream().limit(3).toList();
        } catch (Exception e) {
            log.warn("Could not retrieve products for RAG context: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==========================================
    // HELPER: Smart Domain Fallback Tri Thức Trầm Hương
    // ==========================================
    private String generateDomainFallbackReply(String query, List<ProductResponse> products) {
        String lower = query.toLowerCase();

        // 1. Tư vấn Mệnh Hỏa / Thổ
        if (lower.contains("hỏa") || lower.contains("thổ")) {
            return "🌸 **Kính chào Quý khách! Mộc Hương xin chia sẻ về Phong Thủy Mệnh Hỏa & Mệnh Thổ:**\n\n" +
                    "Theo thuyết Ngũ Hành tương sinh: **Mộc sinh Hỏa**, và **Hỏa sinh Thổ**. Trầm hương là kết tinh tinh hoa Mộc ngàn năm của đại ngàn, mang nguồn năng lượng Dương dồi dào, là vật phẩm phong thủy cát lành bậc nhất cho Quý khách mệnh Hỏa và mệnh Thổ:\n\n" +
                    "- **Mệnh Hỏa:** Đeo vòng trầm giúp điều hòa tính hỏa, giữ tâm trí điềm tĩnh, sáng suốt trong công việc và hanh thông tài vận.\n" +
                    "- **Mệnh Thổ:** Mộc sinh Hỏa trợ Thổ, giúp thu hút vượng khí, bồi đắp nền tảng gia đạo vững vàng và bình an.\n\n" +
                    "✨ **Gợi ý từ Mộc Hương:** Quý khách có thể chọn **Vòng Trầm Hương 108 Hạt Tĩnh Tâm** hoặc **Nụ Trầm Hương Thượng Hạng** xông phòng trà, thư phòng để thu hút trọn vẹn cát khí.";
        }

        // 2. Tư vấn Mệnh Kim / Thủy
        if (lower.contains("kim") || lower.contains("thủy")) {
            return "🌿 **Kính chào Quý khách! Mộc Hương xin chia sẻ về Phong Thủy Mệnh Kim & Thủy:**\n\n" +
                    "Trong phong thủy Đông Phương, trầm hương mộc tự nhiên mang hương thơm thuần khiết, thanh tịnh. Năng lượng dương của Trầm giúp người mệnh Kim và mệnh Thủy xua tan ám khí, tịnh hóa tâm trí và cân bằng cảm xúc:\n\n" +
                    "- **Mệnh Kim:** Nên kết hợp chuỗi Trầm Hương tự nhiên bọc charm bạc hoặc vàng để tương hợp Kim - Mộc cân bằng.\n" +
                    "- **Mệnh Thủy:** Thủy dưỡng Mộc, đeo trầm giúp bồi đắp sinh khí, nuôi dưỡng lòng nhân ái và sự an yên trong tâm hồn.\n\n" +
                    "✨ **Gợi ý từ Mộc Hương:** Dòng sản phẩm **Tinh Dầu Trầm Hương Tinh Khiết** hoặc **Nhang Trầm Không Tăm** rất phù hợp để thanh lọc không gian sống của Quý khách.";
        }

        // 3. Tư vấn Mệnh Mộc
        if (lower.contains("mộc")) {
            return "🍃 **Kính chào Quý khách! Mộc Hương xin chia sẻ về Phong Thủy Mệnh Mộc:**\n\n" +
                    "Quý khách thuộc hành Mộc, khi đồng hành cùng Trầm Hương tự nhiên chính là **Mộc - Mộc tương hòa**, tăng cường gấp bội sinh khí bản mệnh. Năng lượng đại ngàn của trầm hương sẽ bảo bọc Quý khách tránh khỏi tà khí, tăng cường trực giác và sự kiên định trên con đường sự nghiệp.\n\n" +
                    "✨ Tất cả các tác phẩm Vòng Trầm, Nụ Trầm và Nhang Trầm tại Mộc Hương đều rất tương hợp với Quý khách!";
        }

        // 4. Tư vấn Nụ Trầm & Thác Khói
        if (lower.contains("nụ") || lower.contains("thác") || lower.contains("khói")) {
            return "🪵 **Cách Thưởng Trầm Thác Khói Chuẩn Thiền Vị Cùng Mộc Hương:**\n\n" +
                    "1. **Cấu tạo nụ:** Nụ trầm thác khói được chế tác có lỗ dẫn khí rỗng ở giữa. Khi đốt, khói chứa tinh dầu nặng hơn không khí sẽ chảy ngược xuống dưới chân nụ.\n" +
                    "2. **Cách dùng:** Châm lửa đỉnh nụ, để cháy đỏ khoảng 5-10 giây rồi phẩy nhẹ tắt lửa ngọn. Đặt nụ ngay ngắn lên đỉnh thác gốm.\n" +
                    "3. **Thưởng ngoạn:** Đặt thác khói ở nơi kín gió để chiêm ngưỡng làn khói bồng bềnh chảy như suối mây, lan tỏa hương thơm trầm ấm giúp an thần, tĩnh trí.\n\n" +
                    "✨ Mộc Hương đang có sẵn **Nụ Trầm Hương Thượng Hạng** từ bột trầm rừng nguyên chất không phụ gia độc hại.";
        }

        // 5. Chính sách ship, khuyến mãi, bảo hành
        if (lower.contains("ship") || lower.contains("giảm giá") || lower.contains("mã") || lower.contains("vận chuyển") || lower.contains("khuyến mãi")) {
            return "🎁 **Chính Sách Ưu Đãi & Vận Chuyển Của Mộc Hương Shop:**\n\n" +
                    "- **Miễn Phí Vận Chuyển:** Toàn quốc cho tất cả đơn hàng từ **300.000 ₫** (Mã `FREESHIP0D`).\n" +
                    "- **Mã Giảm Giá Tân Duyên:** Nhập mã `TINHTAM10` hoặc `MOCHUONG10` để được **giảm ngay 10%** cho đơn hàng từ 200.000 ₫.\n" +
                    "- **Thời Gian Giao Hàng:** Từ 1 - 2 ngày (Nội thành) và 2 - 4 ngày (Toàn quốc).\n" +
                    "- **Bảo Hành Trọn Đời:** Cam kết đền bù 200% nếu phát hiện trầm giả, trầm ép hương liệu độc hại.";
        }

        // 6. Trả lời chung mặc định giàu thiền vị
        return "🌸 **Mộc Hương xin kính chào Quý khách!**\n\n" +
                "Mộc Hương là không gian chuyên cung cấp các phẩm vật Trầm Hương thiên nhiên thanh tịnh: Vòng tay trầm hương phong thủy, Nụ trầm thác khói, Nhang không tăm thưởng trà và Tinh dầu trầm nguyên chất.\n\n" +
                "Quý khách đang muốn tìm kiếm phẩm vật trầm hương để **tọa thiền, tĩnh tâm, thanh lọc không gian sống** hay **tư vấn chọn vòng tay theo Mệnh phong thủy**? Xin hoan hỷ cho Mộc Hương biết thêm để được chu toàn hỗ trợ Quý khách nhé!";
    }
}

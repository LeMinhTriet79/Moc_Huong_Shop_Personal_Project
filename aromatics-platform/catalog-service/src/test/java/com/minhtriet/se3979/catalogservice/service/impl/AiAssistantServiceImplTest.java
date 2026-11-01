package com.minhtriet.se3979.catalogservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.AiChatRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiGenerateDescriptionRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiSuggestReplyRequest;
import com.minhtriet.se3979.catalogservice.dto.response.AiChatResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiAssistantServiceImpl aiAssistantService;

    @BeforeEach
    void setUp() {
        // Cấu hình fallback mode (không có apiKey thật để test cơ chế Smart Domain RAG Fallback)
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "");
        ReflectionTestUtils.setField(aiAssistantService, "model", "gemini-1.5-flash");
        ReflectionTestUtils.setField(aiAssistantService, "urlTemplate", "https://mock.gemini/{model}?key={apiKey}");
    }

    @Test
    @DisplayName("AI Chat tư vấn mệnh Hỏa & Thổ theo thuyết Ngũ Hành phong thủy")
    void testChat_FengShui_FireAndEarth() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("Tôi mệnh Hỏa thì nên đeo vòng trầm hương nào hợp phong thủy?");

        ProductResponse mockProduct = ProductResponse.builder()
                .id(1L)
                .name("Vòng Trầm Hương 108 Hạt Tĩnh Tâm")
                .basePrice(BigDecimal.valueOf(850000))
                .averageRating(BigDecimal.valueOf(5.0))
                .shortDescription("Vòng trầm hương 108 hạt tự nhiên giúp an thần định khí")
                .build();

        when(productService.searchProducts(eq("vòng"), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockProduct)));

        AiChatResponse response = aiAssistantService.chat(request);

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.getReply().contains("Mệnh Hỏa") || response.getReply().contains("Mộc sinh Hỏa"));
        assertTrue(response.getReply().contains("Ngũ Hành"));
        assertNotNull(response.getSuggestedProducts());
        assertEquals(1, response.getSuggestedProducts().size());
        assertEquals("Vòng Trầm Hương 108 Hạt Tĩnh Tâm", response.getSuggestedProducts().get(0).getName());
    }

    @Test
    @DisplayName("AI Chat hướng dẫn cách đốt nụ trầm thác khói và chính sách freeship")
    void testChat_IncenseCone_And_Policy() {
        // 1. Test hỏi nụ trầm
        AiChatRequest reqCone = new AiChatRequest();
        reqCone.setMessage("Hướng dẫn tôi cách đốt nụ trầm thác khói chảy ngược");

        when(productService.searchProducts(eq("nụ"), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(productService.getFeaturedProducts()).thenReturn(Collections.emptyList());

        AiChatResponse resCone = aiAssistantService.chat(reqCone);
        assertNotNull(resCone);
        assertTrue(resCone.getReply().contains("thác khói") || resCone.getReply().contains("Nụ Trầm"));

        // 2. Test hỏi chính sách mã giảm giá và ship
        AiChatRequest reqShip = new AiChatRequest();
        reqShip.setMessage("Shop có mã giảm giá hay miễn phí ship không?");

        AiChatResponse resShip = aiAssistantService.chat(reqShip);
        assertNotNull(resShip);
        assertTrue(resShip.getReply().contains("FREESHIP0D") || resShip.getReply().contains("TINHTAM10"));
    }

    @Test
    @DisplayName("AI Co-Pilot tự động soạn thảo bài mô tả sản phẩm phong thủy chuẩn SEO")
    void testGenerateProductDescription() {
        AiGenerateDescriptionRequest request = new AiGenerateDescriptionRequest();
        request.setProductName("Trầm Nụ Thác Khói Hoàng Kim");
        request.setCategoryName("Nụ Trầm Hương");
        request.setKeywords(List.of("trầm nụ", "thác khói", "tĩnh tâm"));
        request.setTargetAudience("Người thưởng trà, thiền trà");

        AiChatResponse response = aiAssistantService.generateProductDescription(request);

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.getReply().contains("Trầm Nụ Thác Khói Hoàng Kim"));
        assertTrue(response.getReply().contains("Tầng Hương") || response.getReply().contains("Phong Thủy"));
        assertTrue(response.getReply().contains("Mộc Hương Shop"));
    }

    @Test
    @DisplayName("AI Co-Pilot gợi ý câu phản hồi đánh giá khách hàng (5 sao và 2 sao)")
    void testSuggestReviewReply() {
        // 1. Phản hồi đánh giá 5 sao
        AiSuggestReplyRequest req5Star = new AiSuggestReplyRequest();
        req5Star.setCustomerName("Chị Lan Anh");
        req5Star.setProductName("Vòng Trầm 108 Hạt");
        req5Star.setRating(5);
        req5Star.setComment("Mùi thơm rất ngọt và dịu, đeo vào cảm giác tâm nhẹ nhàng hẳn!");

        AiChatResponse res5Star = aiAssistantService.suggestReviewReply(req5Star);
        assertNotNull(res5Star);
        assertTrue(res5Star.getReply().contains("Chị Lan Anh"));
        assertTrue(res5Star.getReply().contains("tri ân") || res5Star.getReply().contains("an lạc"));

        // 2. Phản hồi khiếu nại 2 sao
        AiSuggestReplyRequest req2Star = new AiSuggestReplyRequest();
        req2Star.setCustomerName("Anh Tuấn");
        req2Star.setProductName("Nụ Trầm Thác Khói");
        req2Star.setRating(2);
        req2Star.setComment("Giao hơi chậm, hộp bị móp nhẹ.");

        AiChatResponse res2Star = aiAssistantService.suggestReviewReply(req2Star);
        assertNotNull(res2Star);
        assertTrue(res2Star.getReply().contains("Anh Tuấn"));
        assertTrue(res2Star.getReply().contains("cáo lỗi") || res2Star.getReply().contains("đổi mới"));
    }
}

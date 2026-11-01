package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.AiChatRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiGenerateDescriptionRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiSuggestReplyRequest;
import com.minhtriet.se3979.catalogservice.dto.response.AiChatResponse;

public interface AiAssistantService {

    AiChatResponse chat(AiChatRequest request);

    AiChatResponse generateProductDescription(AiGenerateDescriptionRequest request);

    AiChatResponse suggestReviewReply(AiSuggestReplyRequest request);
}

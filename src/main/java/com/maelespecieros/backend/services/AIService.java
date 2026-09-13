package com.maelespecieros.backend.services;

import com.maelespecieros.backend.dto.request.ChatRequest;
import com.maelespecieros.backend.dto.response.AIInsightsResponse;
import com.maelespecieros.backend.dto.response.ChatResponse;

public interface AIService {
    AIInsightsResponse getInsights();
    ChatResponse askQuestion(ChatRequest request, String role);
}

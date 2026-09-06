package com.maelespecieros.backend.controllers;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.dto.response.AIInsightsResponse;
import com.maelespecieros.backend.services.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/insights")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<AIInsightsResponse>> getInsights() {
        AIInsightsResponse insights = aiService.getInsights();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Insights generados correctamente",
                insights
        ));
    }
}

package com.maelespecieros.backend.controllers;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.dto.request.ChatRequest;
import com.maelespecieros.backend.dto.response.AIInsightsResponse;
import com.maelespecieros.backend.dto.response.ChatResponse;
import com.maelespecieros.backend.services.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChatResponse>> askQuestion(
            @RequestBody ChatRequest request, 
            Authentication authentication) {
        
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        ChatResponse response = aiService.askQuestion(request, role);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Respuesta generada",
                response
        ));
    }
}

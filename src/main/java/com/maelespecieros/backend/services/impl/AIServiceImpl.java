package com.maelespecieros.backend.services.impl;

import com.maelespecieros.backend.dto.response.AIInsightsResponse;
import com.maelespecieros.backend.dto.response.DashboardResponse;
import com.maelespecieros.backend.services.AIService;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final DashboardService dashboardService;
    private final BlockchainService blockchainService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Override
    public AIInsightsResponse getInsights() {
        boolean systemAltered = !blockchainService.verificarCadena();
        DashboardResponse dashboard = dashboardService.obtenerDashboard();
        
        String prompt = "Actúa como un asistente inteligente de un sistema ERP (Mael Especieros). " +
                "Analiza los siguientes datos y genera un 'insight' o consejo breve y accionable (máximo 2 oraciones): " +
                "Total de productos: " + dashboard.totalProductos() + ", " +
                "Productos activos: " + dashboard.productosActivos() + ", " +
                "Productos con stock bajo: " + dashboard.productosStockBajo() + ", " +
                "Total ventas: " + dashboard.totalVentas() + ", " +
                "Total facturado: $" + dashboard.totalFacturado() + ".";

        String insightResult = "Análisis en curso. Stock bajo requiere atención.";

        if (geminiApiKey != null && !geminiApiKey.equals("su_clave_api_gemini_aqui")) {
            try {
                String listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + geminiApiKey;
                Map modelsResponse = restTemplate.getForObject(listUrl, Map.class);
                
                List<Map<String, Object>> modelsList = (List<Map<String, Object>>) modelsResponse.get("models");
                StringBuilder availableModels = new StringBuilder();
                if (modelsList != null) {
                    for (Map<String, Object> m : modelsList) {
                        String name = (String) m.get("name");
                        if (name.contains("gemini") && name.contains("flash")) {
                            availableModels.append(name).append(", ");
                        }
                    }
                }
                String debugModels = availableModels.toString();

                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + geminiApiKey;
                
                Map<String, Object> requestBody = new HashMap<>();
                Map<String, Object> parts = new HashMap<>();
                parts.put("text", prompt);
                Map<String, Object> contents = new HashMap<>();
                contents.put("parts", List.of(parts));
                requestBody.put("contents", List.of(contents));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                try {
                    Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
                    if (response != null && response.containsKey("candidates")) {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (!candidates.isEmpty()) {
                            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                            List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                            insightResult = resParts.get(0).get("text").toString().trim();
                        }
                    }
                } catch (Exception apiEx) {
                    insightResult = "API Error. Modelos con 'flash' soportados por tu cuenta: [" + debugModels + "]. Detalle: " + apiEx.getMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("GEMINI API ERROR: " + e.getMessage());
                insightResult = "Error IA: Revise consola del backend para ver modelos válidos. (" + e.getMessage() + ")";
            }
        }

        return AIInsightsResponse.builder()
                .insight(insightResult)
                .systemAltered(systemAltered)
                .build();
    }
}

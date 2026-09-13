package com.maelespecieros.backend.services.impl;

import com.maelespecieros.backend.dto.request.ChatRequest;
import com.maelespecieros.backend.dto.response.AIInsightsResponse;
import com.maelespecieros.backend.dto.response.ChatResponse;
import com.maelespecieros.backend.dto.response.DashboardResponse;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.repositories.DetalleVentaRepository;
import com.maelespecieros.backend.repositories.ProductoRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final DashboardService dashboardService;
    private final BlockchainService blockchainService;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final com.maelespecieros.backend.repositories.VentaRepository ventaRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Override
    public AIInsightsResponse getInsights() {
        List<com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse> anomalias = obtenerAnomaliasDelSistema();
        boolean systemAltered = !anomalias.isEmpty();

        DashboardResponse dashboard = dashboardService.obtenerDashboard();
        
        // Cargar estadísticas de ventas
        List<Object[]> salesData = detalleVentaRepository.findSalesByProduct();
        String salesContext = salesData.stream()
                .map(obj -> obj[0] + " vendió " + obj[1] + " unidades")
                .collect(Collectors.joining(", "));

        String anomaliasText = systemAltered ? 
            " ¡ALERTA CRÍTICA DE INTEGRIDAD!: El sistema detectó las siguientes anomalías (probablemente manipulaciones directas en la base de datos o blockchain): " +
            anomalias.stream().map(a -> a.getDescripcion()).collect(Collectors.joining("; ")) : "";

        String prompt = "Actúa como un analista de negocios de un sistema ERP (Mael Especieros). " +
                "Analiza los siguientes datos y genera un 'insight' o recomendación estratégica breve y accionable (máximo 2 oraciones, sé directo). " +
                "Total de productos: " + dashboard.totalProductos() + ", " +
                "Productos activos: " + dashboard.productosActivos() + ", " +
                "Productos con stock bajo: " + dashboard.productosStockBajo() + ", " +
                "Total ventas: " + dashboard.totalVentas() + ", " +
                "Total facturado: $" + dashboard.totalFacturado() + ". " +
                "Ventas por producto: " + salesContext + "." + anomaliasText;

        String insightResult = callGeminiApi(prompt);
        if(insightResult.equals("ERROR")) {
            insightResult = "Error IA: Revise la consola del backend o la API KEY.";
        }

        return AIInsightsResponse.builder()
                .insight(insightResult)
                .systemAltered(systemAltered)
                .build();
    }

    private List<com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse> obtenerAnomaliasDelSistema() {
        List<com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse> anomalias = new java.util.ArrayList<>();
        anomalias.addAll(blockchainService.verificarCadenaDetallado());

        // Revisar Productos
        for (Producto prod : productoRepository.findAll()) {
            if (!prod.esIntegro()) {
                anomalias.add(new com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse(
                    "PRODUCTO", prod.getCodigoProducto(), "Stock / Precio", "Firma Criptográfica Segura", "Datos Modificados Manualmente",
                    "Alerta: El producto " + prod.getNombre() + " (Cod: " + prod.getCodigoProducto() + ") fue alterado directamente en la BD."
                ));
            }
        }

        // Revisar Ventas
        for (com.maelespecieros.backend.entities.Venta venta : ventaRepository.findAll()) {
            if (!venta.esIntegro()) {
                anomalias.add(new com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse(
                    "VENTA", venta.getNumeroVenta(), "Totales / Estado", "Firma Criptográfica Segura", "Montos Alterados",
                    "Alerta: La venta " + venta.getNumeroVenta() + " fue alterada directamente en la BD."
                ));
            }
        }

        return anomalias;
    }

    @Override
    public ChatResponse askQuestion(ChatRequest request, String role) {
        StringBuilder context = new StringBuilder();
        context.append("Eres 'Mael IA', el asistente virtual experto en negocios del ERP Mael Especieros.\n");
        context.append("El usuario que te habla tiene el rol: ").append(role).append(".\n");
        
        List<com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse> anomalias = obtenerAnomaliasDelSistema();
        boolean systemAltered = !anomalias.isEmpty();

        if (role.equals("ROLE_SUPER_ADMIN")) {
            context.append("Estado de Blockchain y Base de Datos: ").append(systemAltered ? "ALTERADA (ALERTA CRÍTICA)" : "Íntegra").append(".\n");
            if (systemAltered) {
                context.append("DETALLES DE LA ALTERACIÓN (Usa esta información si el usuario pregunta qué fue alterado):\n");
                for (com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse a : anomalias) {
                    context.append("- ").append(a.getDescripcion()).append("\n");
                }
            }
        } else {
            context.append("No tienes permiso para revelar información sobre Blockchain o Hashes a este usuario.\n");
        }

        DashboardResponse dashboard = dashboardService.obtenerDashboard();
        context.append("Resumen general del negocio: ").append(dashboard.totalVentas()).append(" ventas, $")
               .append(dashboard.totalFacturado()).append(" facturados.\n");

        context.append("Historial de Unidades Vendidas por Producto (si no aparece, vendió 0):\n");
        List<Object[]> salesData = detalleVentaRepository.findSalesByProduct();
        Map<String, Long> salesMap = new HashMap<>();
        for (Object[] obj : salesData) {
            String prodName = (String) obj[0];
            Long total = (Long) obj[1];
            salesMap.put(prodName, total);
        }

        context.append("Lista del Inventario Actual:\n");
        List<Producto> productos = productoRepository.findAll();
        for (Producto p : productos) {
            long vendidos = salesMap.getOrDefault(p.getNombre(), 0L);
            context.append("- ").append(p.getNombre())
                   .append(" (Stock: ").append(p.getStock())
                   .append(", Costo: $").append(p.getCosto())
                   .append(", Precio: $").append(p.getPrecioEfectivo())
                   .append(", Unidades Vendidas Históricas: ").append(vendidos)
                   .append(")\n");
        }
        
        context.append("\nINSTRUCCIONES CLAVES PARA TI:\n");
        context.append("1. Si el usuario te pide recomendaciones, analiza el costo, el precio y el historial de unidades vendidas.\n");
        context.append("2. Si un producto se vende poco, sugiere promociones (ej. descuentos basados en el margen de ganancia).\n");
        context.append("3. Si un producto se vende mucho, sugiere asegurar stock o aumentar levemente el precio si el margen es bajo.\n");

        context.append("\nEl usuario pregunta: \"").append(request.getMessage()).append("\"\n");
        context.append("Responde de forma concisa, profesional y útil. MUY IMPORTANTE: Limítate a responder ÚNICAMENTE sobre Mael Especieros, su inventario, ventas y datos del negocio. Si el usuario pregunta cosas no relacionadas con esto, responde amablemente que solo puedes ayudar con temas del sistema ERP Mael Especieros.");

        String reply = callGeminiApi(context.toString());
        if(reply.equals("ERROR")) {
            reply = "Lo siento, ha ocurrido un error al comunicarme con la API de IA.";
        }
        
        return ChatResponse.builder().reply(reply).build();
    }

    private String callGeminiApi(String prompt) {
        if (geminiApiKey == null || geminiApiKey.equals("su_clave_api_gemini_aqui")) {
            return "API KEY no configurada.";
        }
        try {
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
            
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                    return resParts.get(0).get("text").toString().trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("GEMINI API ERROR: " + e.getMessage());
        }
        return "ERROR";
    }
}

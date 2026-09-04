package com.maelespecieros.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.dto.response.DashboardResponse;
import com.maelespecieros.backend.services.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> obtenerDashboard() {

        DashboardResponse response =
                service.obtenerDashboard();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Dashboard obtenido correctamente.",
                        response));

    }

}
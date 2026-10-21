package com.maelespecieros.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.dto.request.EgresoCajaRequest;
import com.maelespecieros.backend.dto.response.EgresoCajaResponse;
import com.maelespecieros.backend.services.EgresoCajaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/caja", "/api/cajas"})
@Validated
public class EgresoCajaController {

    private final EgresoCajaService egresoCajaService;

    public EgresoCajaController(EgresoCajaService egresoCajaService) {
        this.egresoCajaService = egresoCajaService;
    }

    @PostMapping("/egresos")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<EgresoCajaResponse>> registrarEgreso(
            @Valid @RequestBody EgresoCajaRequest request) {

        EgresoCajaResponse response = egresoCajaService.registrarEgreso(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Egreso de caja registrado exitosamente.",
                        response));
    }

    @GetMapping("/egresos/actual")
    public ResponseEntity<ApiResponse<List<EgresoCajaResponse>>> listarEgresosActual() {
        List<EgresoCajaResponse> response = egresoCajaService.listarEgresosCajaActual();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Egresos del turno actual obtenidos correctamente.",
                        response));
    }

    @GetMapping("/{cajaId}/egresos")
    public ResponseEntity<ApiResponse<List<EgresoCajaResponse>>> listarEgresosPorCaja(
            @PathVariable Long cajaId) {

        List<EgresoCajaResponse> response = egresoCajaService.listarEgresosPorCaja(cajaId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Egresos de caja obtenidos correctamente.",
                        response));
    }
}

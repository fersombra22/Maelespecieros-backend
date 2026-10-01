package com.maelespecieros.backend.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.common.Messages;
import com.maelespecieros.backend.dto.request.GastoOperativoRequest;
import com.maelespecieros.backend.dto.response.GastoOperativoResponse;
import com.maelespecieros.backend.services.GastoOperativoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gastos")
@Validated
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
public class GastoOperativoController {

    private final GastoOperativoService gastoOperativoService;

    public GastoOperativoController(GastoOperativoService gastoOperativoService) {
        this.gastoOperativoService = gastoOperativoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GastoOperativoResponse>> registrar(
            @Valid @RequestBody GastoOperativoRequest request
    ) {
        GastoOperativoResponse response = gastoOperativoService.registrarGasto(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Gasto operativo registrado exitosamente.", response));
    }

    @GetMapping("/caja-actual")
    public ResponseEntity<ApiResponse<List<GastoOperativoResponse>>> listarGastosCajaActual() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.LIST, gastoOperativoService.listarGastosCajaActual())
        );
    }

    @GetMapping("/caja/{cajaId}")
    public ResponseEntity<ApiResponse<List<GastoOperativoResponse>>> listarGastosPorCaja(
            @PathVariable Long cajaId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.LIST, gastoOperativoService.listarGastosPorCaja(cajaId))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GastoOperativoResponse>>> listarHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.LIST, gastoOperativoService.listarHistorial(pageable))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GastoOperativoResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.FOUND, gastoOperativoService.obtenerPorId(id))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<GastoOperativoResponse>> anular(@PathVariable Long id) {
        GastoOperativoResponse response = gastoOperativoService.anularGasto(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Gasto operativo anulado exitosamente.", response)
        );
    }
}

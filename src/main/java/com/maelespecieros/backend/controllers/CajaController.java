package com.maelespecieros.backend.controllers;

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
import com.maelespecieros.backend.dto.request.AperturaCajaDTO;
import com.maelespecieros.backend.dto.request.CierreCajaDTO;
import com.maelespecieros.backend.dto.response.CajaResponseDTO;
import com.maelespecieros.backend.dto.response.EstadoActualCajaDTO;
import com.maelespecieros.backend.services.CajaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/caja")
@Validated
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping("/estado-actual")
    public ResponseEntity<ApiResponse<EstadoActualCajaDTO>> obtenerEstadoActual() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.FOUND, cajaService.obtenerEstadoActual())
        );
    }

    @PostMapping("/abrir")
    public ResponseEntity<ApiResponse<CajaResponseDTO>> abrir(
            @Valid @RequestBody AperturaCajaDTO request
    ) {
        CajaResponseDTO caja = cajaService.abrirCaja(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Caja abierta exitosamente.", caja));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<ApiResponse<CajaResponseDTO>> cerrar(
            @Valid @RequestBody CierreCajaDTO request
    ) {
        CajaResponseDTO caja = cajaService.cerrarCaja(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Caja cerrada y arqueada exitosamente.", caja)
        );
    }

    @GetMapping("/historial")
    public ResponseEntity<ApiResponse<Page<CajaResponseDTO>>> listarHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaApertura"));
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.LIST, cajaService.listarHistorial(pageable))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CajaResponseDTO>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.FOUND, cajaService.obtenerPorId(id))
        );
    }
}

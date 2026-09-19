package com.maelespecieros.backend.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.common.Messages;
import com.maelespecieros.backend.dto.request.VentaRequest;
import com.maelespecieros.backend.dto.response.VentaResponse;
import com.maelespecieros.backend.dto.response.ComparacionVentasResponse;
import com.maelespecieros.backend.services.VentaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
@Validated
public class VentaController {

    private final VentaService service;

    public VentaController(VentaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VentaResponse>> crear(
            @Valid @RequestBody VentaRequest request) {

        VentaResponse response = service.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        Messages.CREATED,
                        response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VentaResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<VentaResponse> response = service.listar(PageRequest.of(page, size));

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        Messages.LIST,
                        response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaResponse>> obtenerPorId(
            @PathVariable Long id) {

        VentaResponse response = service.obtenerPorId(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        Messages.FOUND,
                        response));
    }

    @GetMapping("/numero/{numeroVenta}")
    public ResponseEntity<ApiResponse<VentaResponse>> obtenerPorNumero(
            @PathVariable String numeroVenta) {

        VentaResponse response =
                service.obtenerPorNumero(numeroVenta);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        Messages.FOUND,
                        response));
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<Void>> anular(
            @PathVariable Long id) {

        service.anular(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Venta anulada correctamente.",
                        null));
    }

    @GetMapping(value = "/{id}/comprobante", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarComprobante(@PathVariable Long id) {
        byte[] pdf = service.generarComprobanteVenta(id);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante_" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/comparacion")
    public ResponseEntity<ApiResponse<ComparacionVentasResponse>> compararVentas(
            @RequestParam(defaultValue = "MES") String periodo) {

        ComparacionVentasResponse response = service.compararVentas(periodo);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Comparación de ventas obtenida correctamente.",
                        response));
    }

}
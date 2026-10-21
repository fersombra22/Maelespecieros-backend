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
import com.maelespecieros.backend.dto.request.ClienteRequest;
import com.maelespecieros.backend.dto.response.ClienteResponse;
import com.maelespecieros.backend.services.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@Validated
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(
            @Valid @RequestBody ClienteRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, Messages.CREATED, service.crear(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClienteResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "apellido") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy, "nombre"));

        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.LIST, service.listar(search, activo, pageable))
        );
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarActivos() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.LIST, service.listarTodosActivos())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.FOUND, service.obtenerPorId(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.UPDATED, service.actualizar(id, request))
        );
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(
            @PathVariable Long id
    ) {
        service.desactivar(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.DELETED, null)
        );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(
            @PathVariable Long id
    ) {
        service.activar(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.UPDATED, null)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id
    ) {
        service.eliminar(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, Messages.DELETED, null)
        );
    }
}

package com.maelespecieros.backend.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse;
import com.maelespecieros.backend.dto.response.BlockchainAuditResponse;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.entities.Venta;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.BlockchainService;

@RestController
@RequestMapping("/api/blockchain")
@PreAuthorize("hasRole('SUPER_ADMIN')") 
public class BlockchainController {

    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping
    public ResponseEntity<Page<BlockchainAuditResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(blockchainService.listarUltimosBloques(pageable));
    }

    @GetMapping("/todos")
    public ResponseEntity<List<BlockchainAuditResponse>> listarTodo(){
        return ResponseEntity.ok(blockchainService.listarCadena());
    }

    /*
     * =====================================================
     * VERIFICACION COMPLETA Y RECONCILIACIÓN DE DATOS
     * (Modificado para Angular: Retorna validez + anomalías)
     * =====================================================
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificar(){
        return ResponseEntity.ok(blockchainService.verificarSistemaCompleto());
    }

    @GetMapping("/verificar/{cantidad}")
    public ResponseEntity<Boolean> verificarUltimos(@PathVariable int cantidad){
        return ResponseEntity.ok(blockchainService.verificarUltimosBloques(cantidad));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<BlockchainAuditResponse> buscarUuid(@PathVariable String uuid){
        BlockchainAuditResponse response = blockchainService.buscarPorUuid(uuid);
        if(response == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hash/{hash}")
    public ResponseEntity<BlockchainAuditResponse> buscarHash(@PathVariable String hash){
        BlockchainAuditResponse response = blockchainService.buscarPorHash(hash);
        if(response == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<BlockchainAuditResponse>> buscar(
            @RequestParam(required = false) LocalDateTime desde,
            @RequestParam(required = false) LocalDateTime hasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String accion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        if(desde == null){
            desde = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if(hasta == null){
            hasta = LocalDateTime.now();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(blockchainService.listarPaginadoYFiltrado(desde, hasta, usuario, accion, pageable));
    }
}
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
@CrossOrigin(originPatterns = "*") 
@PreAuthorize("hasRole('SUPER_ADMIN')") 
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public BlockchainController(
            BlockchainService blockchainService,
            ProductoRepository productoRepository,
            VentaRepository ventaRepository) {
        this.blockchainService = blockchainService;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
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
        
        // 1. Verificamos la inmutabilidad de la cadena (Auditoría)
        boolean cadenaValida = blockchainService.verificarCadena();
        
        // 2. Verificamos la integridad cruzada (Las filas de SQLite)
        List<AnomaliaAuditoriaResponse> anomalias = verificarIntegridadDatosBD();

        // 3. Empaquetamos todo para Angular
        Map<String, Object> response = new HashMap<>();
        
        // El sistema es válido SOLO si la cadena está sana y NO hay anomalías de BD
        boolean sistemaIntegro = cadenaValida && anomalias.isEmpty();
        
        response.put("valida", sistemaIntegro);
        response.put("anomalias", anomalias);

        return ResponseEntity.ok(response);
    }

    /*
     * Método privado que recorre las tablas y caza a los infractores
     */
    private List<AnomaliaAuditoriaResponse> verificarIntegridadDatosBD() {
        List<AnomaliaAuditoriaResponse> anomalias = new ArrayList<>();

        // Revisar Productos
        List<Producto> productos = productoRepository.findAll();
        for (Producto prod : productos) {
            if (!prod.esIntegro()) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "PRODUCTO",
                    prod.getCodigoProducto(),
                    "Stock / Precio",
                    "Firma Criptográfica Segura",
                    "Datos Modificados Manualmente",
                    "Alerta: El registro del producto fue alterado directamente en la base de datos."
                ));
            }
        }

        // Revisar Ventas
        List<Venta> ventas = ventaRepository.findAll();
        for (Venta venta : ventas) {
            if (!venta.esIntegro()) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "VENTA",
                    venta.getNumeroVenta(),
                    "Totales / Estado",
                    "Firma Criptográfica Segura",
                    "Montos Alterados",
                    "Alerta: Los importes o el estado de esta venta fueron modificados ilegalmente."
                ));
            }
        }

        return anomalias;
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
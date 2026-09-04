package com.maelespecieros.backend.services;


import java.time.LocalDateTime;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import com.maelespecieros.backend.dto.response.BlockchainAuditResponse;



public interface BlockchainService {



    /*
     * =====================================================
     *
     * REGISTRAR BLOQUE
     *
     * Cada evento de auditoría genera
     * un bloque inmutable.
     *
     * Mantiene:
     *
     * - hash anterior
     * - hash actual
     * - SHA-256
     * - HMAC
     *
     * =====================================================
     */
    void registrarBloque(
            String usuario,
            String accion,
            String descripcion,
            String entidadTipo,
            String entidadId,
            Object payload
    );







    /*
     * =====================================================
     *
     * VERIFICACIÓN COMPLETA
     *
     * Recorre toda la cadena.
     *
     * Uso administrativo.
     *
     * =====================================================
     */
    boolean verificarCadena();







    /*
     * =====================================================
     *
     * LISTADO COMPLETO
     *
     * Uso exclusivo auditoría.
     *
     * No utilizar para dashboard.
     *
     * =====================================================
     */
    List<BlockchainAuditResponse> listarCadena();







    /*
     * =====================================================
     *
     * LISTADO PAGINADO CON FILTROS
     *
     * Evita cargar miles de registros
     * en memoria.
     *
     * =====================================================
     */
    Page<BlockchainAuditResponse> listarPaginadoYFiltrado(

            LocalDateTime fechaInicio,

            LocalDateTime fechaFin,

            String usuario,

            String accion,

            Pageable pageable

    );







    /*
     * =====================================================
     *
     * ÚLTIMOS BLOQUES
     *
     * Uso principal Angular.
     *
     * Ejemplo:
     *
     * página 0
     * tamaño 5
     *
     * =====================================================
     */
    Page<BlockchainAuditResponse> listarUltimosBloques(

            Pageable pageable

    );







    /*
     * =====================================================
     *
     * VERIFICACIÓN PARCIAL
     *
     * Revisa solamente una cantidad
     * determinada de bloques.
     *
     * Ejemplo:
     *
     * últimos 100
     *
     * =====================================================
     */
    boolean verificarUltimosBloques(

            int cantidad

    );







    /*
     * =====================================================
     *
     * BUSCAR POR UUID
     *
     * =====================================================
     */
    BlockchainAuditResponse buscarPorUuid(

            String uuid

    );







    /*
     * =====================================================
     *
     * BUSCAR POR HASH
     *
     * =====================================================
     */
    BlockchainAuditResponse buscarPorHash(

            String hash

    );



}
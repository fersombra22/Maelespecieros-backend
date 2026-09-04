package com.maelespecieros.backend.repositories;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.maelespecieros.backend.entities.BlockchainAudit;



@Repository
public interface BlockchainAuditRepository 
        extends JpaRepository<BlockchainAudit, Long> {




    /*
     * =====================================================
     *
     * ÚLTIMO BLOQUE
     *
     * Se utiliza para obtener el hash
     * anterior al crear un nuevo bloque.
     *
     * =====================================================
     */
    Optional<BlockchainAudit> findTopByOrderByIdDesc();







    /*
     * =====================================================
     *
     * PAGINACIÓN DASHBOARD
     *
     * Angular no descarga toda la cadena.
     *
     * Ejemplo:
     * últimos 5 bloques.
     *
     * =====================================================
     */
    Page<BlockchainAudit> findAllByOrderByIdDesc(
            Pageable pageable
    );







    /*
     * =====================================================
     *
     * BLOQUE ANTERIOR
     *
     * Utilizado para verificar
     * bloques parciales.
     *
     * =====================================================
     */
    Optional<BlockchainAudit> findTopByIdLessThanOrderByIdDesc(
            Long id
    );







    /*
     * =====================================================
     *
     * BUSCAR UUID
     *
     * =====================================================
     */
    Optional<BlockchainAudit> findByUuid(
            String uuid
    );







    /*
     * =====================================================
     *
     * BUSCAR HASH ACTUAL
     *
     * =====================================================
     */
    Optional<BlockchainAudit> findByHashActual(
            String hashActual
    );







    /*
     * =====================================================
     *
     * FILTROS DE AUDITORÍA
     *
     * Fecha
     * Usuario
     * Acción
     *
     * Con paginación.
     *
     * =====================================================
     */
    Page<BlockchainAudit> 
    findByFechaBetweenAndUsuarioContainingIgnoreCaseAndAccionContainingIgnoreCase(

            LocalDateTime fechaInicio,

            LocalDateTime fechaFin,

            String usuario,

            String accion,

            Pageable pageable

    );







    /*
     * =====================================================
     *
     * CADENA COMPLETA ORDENADA
     *
     * Uso interno.
     *
     * =====================================================
     */
    default List<BlockchainAudit> obtenerCadenaOrdenada(){



        return findAll(

                Sort.by(

                        Sort.Direction.ASC,

                        "id"

                )

        );


    }



}
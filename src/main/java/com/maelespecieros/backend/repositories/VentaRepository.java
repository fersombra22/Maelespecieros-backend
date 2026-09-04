package com.maelespecieros.backend.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.maelespecieros.backend.entities.EstadoVenta;
import com.maelespecieros.backend.entities.Venta;


@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {


    @EntityGraph(attributePaths = {"detalles", "usuario"})
    Page<Venta> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"detalles", "usuario"})
    Optional<Venta> findById(Long id);

    @EntityGraph(attributePaths = {"detalles", "usuario"})
    Optional<Venta> findByNumeroVenta(
            String numeroVenta
    );


    long countByEstado(
            EstadoVenta estado
    );


    @Query("""
            SELECT COALESCE(SUM(v.total),0)
            FROM Venta v
            WHERE v.estado = 'COMPLETADA'
            """)
    BigDecimal obtenerTotalFacturado();



    List<Venta> findByFechaBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );


}
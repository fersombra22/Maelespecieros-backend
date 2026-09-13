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

    @Query("""
            SELECT COALESCE(SUM(v.total), 0)
            FROM Venta v
            WHERE v.estado = 'COMPLETADA'
            AND v.fecha >= :inicio AND v.fecha <= :fin
            """)
    BigDecimal obtenerTotalFacturadoEntreFechas(LocalDateTime inicio, LocalDateTime fin);

    @Query("""
            SELECT new com.maelespecieros.backend.dto.response.VentasPorMetodoPagoResponse(v.formaPago, SUM(v.total))
            FROM Venta v
            WHERE v.estado = 'COMPLETADA'
            GROUP BY v.formaPago
            """)
    List<com.maelespecieros.backend.dto.response.VentasPorMetodoPagoResponse> obtenerVentasPorMetodoPago();

    @Query("""
            SELECT new com.maelespecieros.backend.dto.response.TopProductoResponse(p.nombre, SUM(d.cantidad), SUM(d.subtotal))
            FROM DetalleVenta d
            JOIN d.venta v
            JOIN d.producto p
            WHERE v.estado = 'COMPLETADA'
            GROUP BY p.nombre
            ORDER BY SUM(d.cantidad) DESC
            LIMIT 5
            """)
    List<com.maelespecieros.backend.dto.response.TopProductoResponse> obtenerTopProductos();


}
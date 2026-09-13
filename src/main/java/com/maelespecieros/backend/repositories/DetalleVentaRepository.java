package com.maelespecieros.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.maelespecieros.backend.entities.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);

    @Query("SELECT d.producto.nombre, SUM(d.cantidad) FROM DetalleVenta d JOIN d.venta v WHERE v.estado = 'COMPLETADA' GROUP BY d.producto.nombre")
    List<Object[]> findSalesByProduct();
}
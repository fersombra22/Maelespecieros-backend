package com.maelespecieros.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maelespecieros.backend.entities.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);

}
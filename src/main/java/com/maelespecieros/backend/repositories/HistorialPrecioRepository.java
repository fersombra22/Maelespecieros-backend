package com.maelespecieros.backend.repositories;

import com.maelespecieros.backend.entities.HistorialPrecio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialPrecioRepository extends JpaRepository<HistorialPrecio, Long> {
    List<HistorialPrecio> findByProductoIdOrderByFechaCambioDesc(Long productoId);
}

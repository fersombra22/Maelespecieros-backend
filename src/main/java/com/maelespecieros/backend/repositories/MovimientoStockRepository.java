package com.maelespecieros.backend.repositories;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maelespecieros.backend.entities.MovimientoStock;



@Repository
public interface MovimientoStockRepository 
        extends JpaRepository<MovimientoStock, Long> {



    Page<MovimientoStock> findAllByOrderByFechaDesc(
            Pageable pageable);



    Page<MovimientoStock> findByProductoIdOrderByFechaDesc(
            Long productoId,
            Pageable pageable);



    Optional<MovimientoStock> findTopByProductoIdOrderByIdDesc(
            Long productoId
    );


}
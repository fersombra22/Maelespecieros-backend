package com.maelespecieros.backend.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maelespecieros.backend.entities.Caja;
import com.maelespecieros.backend.entities.EstadoCaja;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Caja> findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja estado);

    boolean existsByEstado(EstadoCaja estado);

    @EntityGraph(attributePaths = {"usuario"})
    Page<Caja> findAllByOrderByFechaAperturaDesc(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Caja> findById(Long id);
}

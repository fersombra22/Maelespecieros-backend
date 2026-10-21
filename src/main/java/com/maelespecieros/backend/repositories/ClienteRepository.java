package com.maelespecieros.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maelespecieros.backend.entities.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Cliente> findByActivoTrueOrderByApellidoAscNombreAsc();

    List<Cliente> findAllByOrderByApellidoAscNombreAsc();

    Page<Cliente> findByActivoTrue(Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:activo IS NULL OR c.activo = :activo) AND " +
           "(:termino IS NULL OR :termino = '' OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(c.telefono) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Cliente> buscarConFiltros(
            @Param("termino") String termino,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    long countByActivoTrue();
}

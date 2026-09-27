package com.maelespecieros.backend.repositories;


import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.maelespecieros.backend.entities.Producto;



@Repository
public interface ProductoRepository 
        extends JpaRepository<Producto, Long> {





    /*
     * Buscar producto por código único
     */
    @EntityGraph(attributePaths = {"categoria"})
    Optional<Producto> findByCodigoProducto(
            String codigoProducto
    );







    /*
     * Validar existencia de código
     */
    boolean existsByCodigoProducto(
            String codigoProducto
    );







    /*
     * Productos activos
     *
     * Uso interno
     */
    List<Producto> findByActivoTrue();







    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"categoria"})
    Optional<Producto> findById(Long id);

    /*
     * Listado paginado producción
     *
     * Ej:
     *
     * página 0
     * tamaño 20
     */
    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findByActivoTrue(
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findByActivoFalse(
            Pageable pageable
    );







    /*
     * Buscar por término (nombre, modelo, código o categoría)
     */
    @EntityGraph(attributePaths = {"categoria"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Producto p WHERE p.activo = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.modelo) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.categoria.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Producto> buscarPorTermino(
            @org.springframework.data.repository.query.Param("termino") String termino,
            Pageable pageable
    );







    /*
     * Productos activos por categoría
     */
    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findByCategoriaIdAndActivoTrue(
            Long categoriaId,
            Pageable pageable
    );







    /*
     * Cantidad total productos activos
     *
     * Dashboard
     */
    long countByActivoTrue();







    /*
     * Productos con stock bajo
     *
     * Dashboard
     */
    long countByStockLessThanEqualAndActivoTrue(
            Integer stock
    );



}
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

import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.GastoOperativo;

@Repository
public interface GastoOperativoRepository extends JpaRepository<GastoOperativo, Long> {

    @EntityGraph(attributePaths = {"caja", "usuario"})
    @Override
    Optional<GastoOperativo> findById(Long id);

    @EntityGraph(attributePaths = {"caja", "usuario"})
    Page<GastoOperativo> findAllByOrderByFechaDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"caja", "usuario"})
    List<GastoOperativo> findByCajaIdOrderByFechaDesc(Long cajaId);

    @EntityGraph(attributePaths = {"caja", "usuario"})
    List<GastoOperativo> findByCajaIdAndAnuladoFalseOrderByFechaDesc(Long cajaId);

    @Query("""
            SELECT COALESCE(SUM(g.monto), 0)
            FROM GastoOperativo g
            WHERE g.caja.id = :cajaId AND g.anulado = false
            """)
    BigDecimal obtenerTotalEgresosPorCaja(Long cajaId);

    @Query("""
            SELECT COALESCE(SUM(g.monto), 0)
            FROM GastoOperativo g
            WHERE g.caja.id = :cajaId AND g.formaPago = :formaPago AND g.anulado = false
            """)
    BigDecimal obtenerTotalEgresosPorCajaYFormaPago(Long cajaId, FormaPago formaPago);

    @Query("""
            SELECT COALESCE(SUM(g.monto), 0)
            FROM GastoOperativo g
            WHERE g.fecha >= :inicio AND g.fecha <= :fin AND g.anulado = false
            """)
    BigDecimal obtenerTotalEgresosEntreFechas(LocalDateTime inicio, LocalDateTime fin);

    @Query("""
            SELECT COALESCE(SUM(g.monto), 0)
            FROM GastoOperativo g
            WHERE g.fecha >= :inicio AND g.fecha <= :fin AND g.formaPago = :formaPago AND g.anulado = false
            """)
    BigDecimal obtenerTotalEgresosEntreFechasYFormaPago(LocalDateTime inicio, LocalDateTime fin, FormaPago formaPago);
}

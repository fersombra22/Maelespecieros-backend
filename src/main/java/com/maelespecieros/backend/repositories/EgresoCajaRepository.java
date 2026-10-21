package com.maelespecieros.backend.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maelespecieros.backend.entities.EgresoCaja;
import com.maelespecieros.backend.entities.TipoMedioEgreso;

@Repository
public interface EgresoCajaRepository extends JpaRepository<EgresoCaja, Long> {

    List<EgresoCaja> findByCajaIdOrderByFechaDesc(Long cajaId);

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM EgresoCaja e WHERE e.caja.id = :cajaId AND e.tipoMedio = :tipoMedio")
    BigDecimal sumarPorCajaYTipoMedio(@Param("cajaId") Long cajaId, @Param("tipoMedio") TipoMedioEgreso tipoMedio);

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM EgresoCaja e WHERE e.caja.id = :cajaId")
    BigDecimal sumarTotalPorCaja(@Param("cajaId") Long cajaId);

    List<EgresoCaja> findByHashIntegridadIsNull();

}

package com.maelespecieros.backend.mappers;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.maelespecieros.backend.dto.response.CajaResponseDTO;
import com.maelespecieros.backend.dto.response.EstadoActualCajaDTO;
import com.maelespecieros.backend.entities.Caja;

@Component
public class CajaMapper {

    public CajaResponseDTO toDTO(
            Caja caja,
            BigDecimal totalEfectivo,
            BigDecimal totalDebito,
            BigDecimal totalCredito,
            BigDecimal totalTransferencia,
            BigDecimal totalDigital,
            Long cantidadVentas
    ) {
        if (caja == null) {
            return null;
        }

        Long usuarioId = null;
        String usuarioUsername = null;
        String usuarioNombre = null;

        if (caja.getUsuario() != null) {
            usuarioId = caja.getUsuario().getId();
            usuarioUsername = caja.getUsuario().getUsername();
            usuarioNombre = caja.getUsuario().getNombre();
        }

        return new CajaResponseDTO(
                caja.getId(),
                caja.getMontoInicial(),
                caja.getMontoFinal(),
                caja.getMontoVentas(),
                caja.getDiferencia(),
                caja.getFechaApertura(),
                caja.getFechaCierre(),
                caja.getEstado(),
                caja.getObservaciones(),
                usuarioId,
                usuarioUsername,
                usuarioNombre,
                totalEfectivo != null ? totalEfectivo : BigDecimal.ZERO,
                totalDebito != null ? totalDebito : BigDecimal.ZERO,
                totalCredito != null ? totalCredito : BigDecimal.ZERO,
                totalTransferencia != null ? totalTransferencia : BigDecimal.ZERO,
                totalDigital != null ? totalDigital : BigDecimal.ZERO,
                cantidadVentas != null ? cantidadVentas : 0L
        );
    }

    public CajaResponseDTO toDTO(Caja caja) {
        return toDTO(caja, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }

    public EstadoActualCajaDTO toEstadoActualDTO(
            boolean abierta,
            Caja caja,
            BigDecimal ventasActuales,
            BigDecimal montoEsperado,
            BigDecimal totalEfectivoActual,
            BigDecimal totalDebitoActual,
            BigDecimal totalCreditoActual,
            BigDecimal totalTransferenciaActual,
            BigDecimal totalDigitalActual,
            Long cantidadVentasActual
    ) {
        return new EstadoActualCajaDTO(
                abierta,
                toDTO(caja, totalEfectivoActual, totalDebitoActual, totalCreditoActual, totalTransferenciaActual, totalDigitalActual, cantidadVentasActual),
                ventasActuales,
                montoEsperado,
                totalEfectivoActual,
                totalDebitoActual,
                totalCreditoActual,
                totalTransferenciaActual,
                totalDigitalActual,
                cantidadVentasActual
        );
    }
}

package com.maelespecieros.backend.mappers;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.maelespecieros.backend.dto.response.CajaResponseDTO;
import com.maelespecieros.backend.dto.response.EstadoActualCajaDTO;
import com.maelespecieros.backend.entities.Caja;

@Component
public class CajaMapper {

    public CajaResponseDTO toDTO(Caja caja) {
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
                usuarioNombre
        );
    }

    public EstadoActualCajaDTO toEstadoActualDTO(boolean abierta, Caja caja, BigDecimal ventasActuales, BigDecimal montoEsperado) {
        return new EstadoActualCajaDTO(
                abierta,
                toDTO(caja),
                ventasActuales,
                montoEsperado
        );
    }
}

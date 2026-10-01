package com.maelespecieros.backend.mappers;

import org.springframework.stereotype.Component;

import com.maelespecieros.backend.dto.response.GastoOperativoResponse;
import com.maelespecieros.backend.entities.GastoOperativo;

@Component
public class GastoOperativoMapper {

    public GastoOperativoResponse toDTO(GastoOperativo gasto) {
        if (gasto == null) {
            return null;
        }

        Long cajaId = gasto.getCaja() != null ? gasto.getCaja().getId() : null;
        Long usuarioId = null;
        String usuarioUsername = null;
        String usuarioNombre = null;

        if (gasto.getUsuario() != null) {
            usuarioId = gasto.getUsuario().getId();
            usuarioUsername = gasto.getUsuario().getUsername();
            usuarioNombre = gasto.getUsuario().getNombre();
        }

        return new GastoOperativoResponse(
                gasto.getId(),
                gasto.getMonto(),
                gasto.getConcepto(),
                gasto.getCategoriaGasto(),
                gasto.getFormaPago(),
                gasto.getComprobanteNro(),
                gasto.getFecha(),
                gasto.getAnulado(),
                cajaId,
                usuarioId,
                usuarioUsername,
                usuarioNombre
        );
    }
}

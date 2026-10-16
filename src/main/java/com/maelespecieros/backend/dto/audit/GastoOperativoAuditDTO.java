package com.maelespecieros.backend.dto.audit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.maelespecieros.backend.entities.GastoOperativo;

public record GastoOperativoAuditDTO(
        Long id,
        BigDecimal monto,
        String concepto,
        String categoriaGasto,
        String formaPago,
        String comprobanteNro,
        LocalDateTime fecha,
        Boolean anulado,
        Long cajaId,
        Long usuarioId,
        String usuarioUsername,
        String hashIntegridad
) {
    public static GastoOperativoAuditDTO fromEntity(GastoOperativo g) {
        if (g == null) {
            return null;
        }
        Long cajaId = g.getCaja() != null ? g.getCaja().getId() : null;
        Long usuarioId = null;
        String usuarioUsername = null;
        if (g.getUsuario() != null) {
            usuarioId = g.getUsuario().getId();
            usuarioUsername = g.getUsuario().getUsername();
        }
        return new GastoOperativoAuditDTO(
                g.getId(),
                g.getMonto(),
                g.getConcepto(),
                g.getCategoriaGasto() != null ? g.getCategoriaGasto().name() : null,
                g.getFormaPago() != null ? g.getFormaPago().name() : null,
                g.getComprobanteNro(),
                g.getFecha(),
                g.getAnulado(),
                cajaId,
                usuarioId,
                usuarioUsername,
                g.getHashIntegridad()
        );
    }
}

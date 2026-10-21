package com.maelespecieros.backend.entities;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "egresos_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EgresoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medio", nullable = false, length = 30)
    private TipoMedioEgreso tipoMedio;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(length = 100)
    private String comprobante;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "hash_integridad", length = 64)
    private String hashIntegridad;

    @Transient
    private final String SECRET_KEY = "MaelEspecierosEgresoSecret2026";

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    public void firmarIntegridad() {
        this.hashIntegridad = calcularHash();
    }

    public boolean esIntegro() {
        if (this.hashIntegridad == null) return false;
        return this.hashIntegridad.equals(calcularHash());
    }

    private String calcularHash() {
        try {
            String fechaStr = this.fecha != null 
                    ? this.fecha.truncatedTo(ChronoUnit.SECONDS).toString() 
                    : "";
            String montoStr = this.monto != null 
                    ? this.monto.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() 
                    : "0.00";
            String cajaIdStr = (this.caja != null && this.caja.getId() != null) ? this.caja.getId().toString() : "0";
            String usuarioIdStr = (this.usuario != null && this.usuario.getId() != null) ? this.usuario.getId().toString() : "0";
            String tipoStr = this.tipoMedio != null ? this.tipoMedio.name() : "";
            String motivoStr = this.motivo != null ? this.motivo.trim() : "";

            String datosVitales = cajaIdStr + "|" +
                                  usuarioIdStr + "|" +
                                  montoStr + "|" +
                                  tipoStr + "|" +
                                  motivoStr + "|" +
                                  fechaStr;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((datosVitales + SECRET_KEY).getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular hash de integridad de egreso", e);
        }
    }
}

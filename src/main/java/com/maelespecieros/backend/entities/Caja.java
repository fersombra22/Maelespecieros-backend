package com.maelespecieros.backend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cajas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_final", precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "monto_ventas", precision = 10, scale = 2)
    private BigDecimal montoVentas;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCaja estado;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /*
     * Sello criptográfico de integridad de la fila.
     * Protege contra manipulaciones directas en la base de datos.
     */
    @Column(name = "hash_integridad", length = 64)
    private String hashIntegridad;

    /*
     * ==========================================
     * METODOS DE INTEGRIDAD FORENSE
     * ==========================================
     */
    @Transient
    private final String SECRET_KEY = "MaelEspecierosSecretKey2026";

    public void firmarIntegridad() {
        this.hashIntegridad = calcularHash();
    }

    public boolean esIntegro() {
        if (this.hashIntegridad == null) return false;
        return this.hashIntegridad.equals(calcularHash());
    }

    private String calcularHash() {
        try {
            String fechaAperturaStr = this.fechaApertura != null 
                    ? this.fechaApertura.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString() 
                    : "";
            String montoInicialStr = this.montoInicial != null ? this.montoInicial.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String montoFinalStr = this.montoFinal != null ? this.montoFinal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String montoVentasStr = this.montoVentas != null ? this.montoVentas.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String diferenciaStr = this.diferencia != null ? this.diferencia.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String usuarioId = (this.usuario != null && this.usuario.getId() != null) ? this.usuario.getId().toString() : "0";

            String datosVitales = fechaAperturaStr + "|" +
                                  montoInicialStr + "|" +
                                  montoFinalStr + "|" +
                                  montoVentasStr + "|" +
                                  diferenciaStr + "|" +
                                  (this.estado != null ? this.estado.name() : "") + "|" +
                                  usuarioId;

            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((datosVitales + SECRET_KEY).getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular la integridad de la caja", e);
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaApertura == null) {
            this.fechaApertura = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        } else {
            this.fechaApertura = this.fechaApertura.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        }
        if (this.estado == null) {
            this.estado = EstadoCaja.ABIERTA;
        }
        if (this.montoVentas == null) {
            this.montoVentas = BigDecimal.ZERO;
        }
        if (this.diferencia == null) {
            this.diferencia = BigDecimal.ZERO;
        }
        firmarIntegridad();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.fechaCierre != null) {
            this.fechaCierre = this.fechaCierre.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        }
        firmarIntegridad();
    }
}

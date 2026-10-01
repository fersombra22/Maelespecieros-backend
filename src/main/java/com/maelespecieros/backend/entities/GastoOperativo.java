package com.maelespecieros.backend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gastos_operativos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GastoOperativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 250)
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_gasto", nullable = false, length = 50)
    private CategoriaGasto categoriaGasto;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", nullable = false, length = 20)
    private FormaPago formaPago;

    @Column(name = "comprobante_nro", length = 50)
    private String comprobanteNro;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean anulado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

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
            String montoStr = this.monto != null ? this.monto.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String cajaIdStr = (this.caja != null && this.caja.getId() != null) ? this.caja.getId().toString() : "0";
            String usuarioIdStr = (this.usuario != null && this.usuario.getId() != null) ? this.usuario.getId().toString() : "0";

            String datosVitales = (this.id != null ? this.id.toString() : "0") + "|" +
                                  montoStr + "|" +
                                  (this.concepto != null ? this.concepto : "") + "|" +
                                  (this.categoriaGasto != null ? this.categoriaGasto.name() : "") + "|" +
                                  (this.formaPago != null ? this.formaPago.name() : "") + "|" +
                                  cajaIdStr + "|" +
                                  usuarioIdStr + "|" +
                                  (this.anulado != null ? this.anulado.toString() : "false");

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
            throw new RuntimeException("Error al calcular la integridad del gasto operativo", e);
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.anulado == null) {
            this.anulado = false;
        }
        firmarIntegridad();
    }

    @PreUpdate
    public void preUpdate() {
        firmarIntegridad();
    }
}

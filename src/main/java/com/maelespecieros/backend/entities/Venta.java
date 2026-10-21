package com.maelespecieros.backend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venta", nullable = false, unique = true, length = 20)
    private String numeroVenta;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPago formaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVenta estado;

    /*
     * Sello criptográfico de integridad de la fila.
     */
    @Column(name = "hash_integridad", length = 64)
    private String hashIntegridad;

    // Usuario que realizó la venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Cliente al que se le realizó la venta (puede ser null para Consumidor Final)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

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
        String hashCalculado = calcularHash();
        if (this.hashIntegridad.equals(hashCalculado)) return true;
        return this.hashIntegridad.equals(calcularHashLegacy());
    }

    private String calcularHash() {
        try {
            String subtotalStr = this.subtotal != null ? this.subtotal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String descuentoStr = this.descuento != null ? this.descuento.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String totalStr = this.total != null ? this.total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String numeroVentaVital = this.numeroVenta != null ? this.numeroVenta : "";
            String clienteVital = this.cliente != null && this.cliente.getId() != null ? this.cliente.getId().toString() : "CONSUMIDOR_FINAL";
            String formaPagoVital = this.formaPago != null ? this.formaPago.name() : "EFECTIVO";

            String datosVitales = numeroVentaVital + "|" + 
                                  subtotalStr + "|" + 
                                  descuentoStr + "|" +
                                  totalStr + "|" + 
                                  formaPagoVital + "|" +
                                  (this.estado != null ? this.estado.name() : "") + "|" +
                                  clienteVital;

            return generarSha256(datosVitales);
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular la integridad de la venta", e);
        }
    }

    private String calcularHashLegacy() {
        try {
            String subtotalStr = this.subtotal != null ? this.subtotal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String totalStr = this.total != null ? this.total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String numeroVentaVital = this.numeroVenta != null ? this.numeroVenta : "";
            String clienteVital = this.cliente != null && this.cliente.getId() != null ? this.cliente.getId().toString() : "CONSUMIDOR_FINAL";
            
            String datosVitales = numeroVentaVital + "|" + 
                                  subtotalStr + "|" + 
                                  totalStr + "|" + 
                                  (this.estado != null ? this.estado.name() : "") + "|" +
                                  clienteVital;

            return generarSha256(datosVitales);
        } catch (Exception e) {
            return "";
        }
    }

    private String generarSha256(String datosVitales) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest((datosVitales + SECRET_KEY).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @PrePersist
    public void prePersist() {
        fecha = LocalDateTime.now();

        if (estado == null) {
            estado = EstadoVenta.COMPLETADA;
        }

        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }

        firmarIntegridad();
    }

    @PreUpdate
    public void preUpdate() {
        firmarIntegridad();
    }
}
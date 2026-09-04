package com.maelespecieros.backend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_producto", nullable = false, unique = true, length = 50)
    private String codigoProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String modelo;

    /*
     * Precio base del producto (Efectivo / transferencia)
     */
    @Column(name = "precio_efectivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioEfectivo;

    /*
     * Costo interno
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    /*
     * Stock actual
     */
    @Column(nullable = false)
    private Integer stock;

    /*
     * Stock mínimo para alertas
     */
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    /*
     * Producto activo
     */
    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /*
     * Sello criptográfico de integridad de la fila.
     * Protege contra alteraciones directas en SQLite.
     */
    @Column(name = "hash_integridad", length = 64)
    private String hashIntegridad;

    // Acá está la corrección para evitar el error 500 al pasar a JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

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
            // Unimos los datos vitales que no queremos que nos alteren por detrás
            String precioEfectivoStr = this.precioEfectivo != null ? this.precioEfectivo.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            
            String datosVitales = this.codigoProducto + "|" + 
                                  precioEfectivoStr + "|" + 
                                  this.stock + "|" + 
                                  this.activo;

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
            throw new RuntimeException("Error al calcular la integridad del producto", e);
        }
    }

    @PrePersist
    public void prePersist(){
        fechaAlta = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();

        if(activo == null){
            activo = true;
        }
        if(stock == null){
            stock = 0;
        }
        if(stockMinimo == null){
            stockMinimo = 0;
        }
        if(costo == null){
            costo = BigDecimal.ZERO;
        }

        // Sellamos la fila antes de crearla en la BD
        firmarIntegridad();
    }

    @PreUpdate
    public void preUpdate(){
        fechaActualizacion = LocalDateTime.now();
        
        // Resellamos la fila si se actualizó legítimamente desde el sistema
        firmarIntegridad();
    }
}
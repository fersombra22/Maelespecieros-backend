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
}

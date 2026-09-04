package com.maelespecieros.backend.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "numeradores")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Numerador {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;





    /*
     * Identificador del contador.
     *
     * Ej:
     *
     * ESP
     * MADERA
     * VENTA
     */
    @Column(
            nullable = false,
            unique = true,
            length = 30
    )
    private String tipo;






    /*
     * Último número utilizado.
     */
    @Column(
            nullable = false
    )
    private Integer ultimoNumero = 0;



}
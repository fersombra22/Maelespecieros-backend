package com.maelespecieros.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Column(nullable = false, unique = true, length = 5)
    private String prefijo;

    @Column(nullable = false)
    private Integer ultimoNumero = 0;

    @Column(nullable = false)
    private Boolean activo = true;
}
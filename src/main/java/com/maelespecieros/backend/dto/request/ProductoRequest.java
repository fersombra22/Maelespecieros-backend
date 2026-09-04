package com.maelespecieros.backend.dto.request;


import java.math.BigDecimal;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



public record ProductoRequest(



        @NotBlank(
                message = "El nombre es obligatorio."
        )
        @Size(max = 100)
        String nombre,



        @Size(max = 300)
        String descripcion,



        @NotBlank(
                message = "El modelo es obligatorio."
        )
        @Size(max = 100)
        String modelo,



        @NotNull(
                message = "El precio efectivo es obligatorio."
        )
        @DecimalMin(
                value = "0.00"
        )
        BigDecimal precioEfectivo,



        @DecimalMin(
                value = "0.00"
        )
        BigDecimal costo,



        @NotNull
        @Min(0)
        Integer stock,



        @NotNull
        @Min(0)
        Integer stockMinimo,



        @NotNull
        Long categoriaId



){

}
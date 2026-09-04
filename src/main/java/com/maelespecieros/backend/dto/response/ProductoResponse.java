package com.maelespecieros.backend.dto.response;


import java.math.BigDecimal;



public record ProductoResponse(


        Long id,


        String codigoProducto,


        String nombre,


        String descripcion,


        String modelo,


        BigDecimal precioEfectivo,


        BigDecimal costo,


        Integer stock,


        Integer stockMinimo,


        Long categoriaId,


        String categoria,


        boolean activo



){

}
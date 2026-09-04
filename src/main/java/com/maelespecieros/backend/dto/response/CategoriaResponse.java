package com.maelespecieros.backend.dto.response;

public record CategoriaResponse(

        Long id,
        String nombre,
        String descripcion,
        String prefijo,
        Integer ultimoNumero,
        boolean activo

) {
}
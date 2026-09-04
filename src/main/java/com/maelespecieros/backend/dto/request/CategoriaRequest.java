package com.maelespecieros.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "El nombre de la categoría es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
        String nombre,

        @Size(max = 250, message = "La descripción no puede superar los 250 caracteres.")
        String descripcion,

        @NotBlank(message = "El prefijo es obligatorio.")
        @Size(min = 2, max = 5, message = "El prefijo debe tener entre 2 y 5 caracteres.")
        String prefijo

) {
}
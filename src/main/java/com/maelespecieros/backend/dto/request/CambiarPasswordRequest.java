package com.maelespecieros.backend.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;



public record CambiarPasswordRequest(


        @NotBlank(message = "El usuario es obligatorio.")
        String username,


        @NotBlank(message = "La contraseña actual es obligatoria.")
        String passwordActual,


        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres.")
        String passwordNueva



) {

}
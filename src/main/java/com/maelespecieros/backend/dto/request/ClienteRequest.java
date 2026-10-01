package com.maelespecieros.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 80, message = "El apellido no puede superar los 80 caracteres.")
        String apellido,

        @NotBlank(message = "El email es obligatorio.")
        @Email(message = "El formato del email no es válido.")
        @Size(max = 120, message = "El email no puede superar los 120 caracteres.")
        String email,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres.")
        String telefono,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres.")
        String direccion,

        Boolean activo

) {
}

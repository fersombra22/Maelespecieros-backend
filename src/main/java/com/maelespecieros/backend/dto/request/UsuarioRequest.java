package com.maelespecieros.backend.dto.request;


import com.maelespecieros.backend.entities.Rol;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



public record UsuarioRequest(



        /*
         * Nombre completo
         */
        @NotBlank(
                message = "El nombre es obligatorio."
        )
        @Size(
                max = 80,
                message = "El nombre no puede superar los 80 caracteres."
        )
        String nombre,





        /*
         * Usuario para login
         */
        @NotBlank(
                message = "El usuario es obligatorio."
        )
        @Size(
                min = 4,
                max = 50,
                message = "El usuario debe tener entre 4 y 50 caracteres."
        )
        String username,





        /*
         * Password inicial.
         *
         * Puede venir vacío
         * en actualizaciones.
         */
        @Size(
                min = 8,
                max = 100,
                message = "La contraseña debe tener entre 8 y 100 caracteres."
        )
        String password,





        /*
         * Rol asignado.
         */
        @NotNull(
                message = "El rol es obligatorio."
        )
        Rol rol



) {


}

package com.maelespecieros.backend.dto.response;

import java.time.LocalDateTime;

public record ClienteResponse(

        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String direccion,
        boolean activo,
        LocalDateTime fechaAlta,
        LocalDateTime fechaActualizacion

) {
}

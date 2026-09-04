package com.maelespecieros.backend.dto.response;


import java.time.LocalDateTime;


import com.maelespecieros.backend.entities.Rol;



public record UsuarioResponse(


        Long id,


        String nombre,


        String username,


        Rol rol,



        /*
         * Usuario habilitado
         */
        Boolean activo,



        /*
         * Bloqueo por seguridad
         */
        Boolean bloqueado,



        /*
         * Intentos fallidos actuales
         */
        Integer intentosFallidos,



        /*
         * Obliga cambio de contraseña
         */
        Boolean cambioPasswordPendiente,



        /*
         * Fecha creación
         */
        LocalDateTime fechaAlta,



        /*
         * Último login correcto
         */
        LocalDateTime ultimoLogin,



        /*
         * Fecha bloqueo
         */
        LocalDateTime fechaBloqueo,



        /*
         * Última modificación contraseña
         */
        LocalDateTime ultimoCambioPassword



) {


}
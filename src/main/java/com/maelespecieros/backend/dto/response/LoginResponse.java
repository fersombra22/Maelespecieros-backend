package com.maelespecieros.backend.dto.response;


import com.maelespecieros.backend.entities.Rol;


public record LoginResponse(


        Long id,


        String nombre,


        String username,


        Rol rol,


        String token,


        Boolean requiereCambioPassword


) {


}
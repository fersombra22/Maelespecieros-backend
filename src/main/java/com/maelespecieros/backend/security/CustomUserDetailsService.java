package com.maelespecieros.backend.security;


import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.repositories.UsuarioRepository;



@Service
public class CustomUserDetailsService 
        implements UserDetailsService {



    private final UsuarioRepository repository;



    public CustomUserDetailsService(
            UsuarioRepository repository
    ){

        this.repository = repository;

    }







    @Override
    public UserDetails loadUserByUsername(
            String username
    )
            throws UsernameNotFoundException {



        Usuario usuario =

                repository
                .findByUsernameAndActivoTrueAndBloqueadoFalse(username)

                .orElseThrow(

                        () ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado o bloqueado."
                        )

                );







        return User.builder()

                .username(
                        usuario.getUsername()
                )

                .password(
                        usuario.getPassword()
                )

                .roles(
                        usuario.getRol().name()
                )

                .disabled(
                        !usuario.getActivo()
                )

                .build();


    }



}
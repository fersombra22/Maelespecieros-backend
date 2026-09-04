package com.maelespecieros.backend.services.impl;


import java.time.LocalDateTime;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.request.CambiarPasswordRequest;
import com.maelespecieros.backend.dto.request.LoginRequest;
import com.maelespecieros.backend.dto.response.LoginResponse;

import com.maelespecieros.backend.entities.Usuario;

import com.maelespecieros.backend.exceptions.BusinessException;

import com.maelespecieros.backend.repositories.UsuarioRepository;

import com.maelespecieros.backend.security.JwtService;

import com.maelespecieros.backend.services.AuthService;
import com.maelespecieros.backend.services.BlockchainService;



@Service
@Transactional
public class AuthServiceImpl implements AuthService {




    private final UsuarioRepository repository;


    private final PasswordEncoder passwordEncoder;


    private final JwtService jwtService;

    private final BlockchainService blockchainService;





    private static final int MAX_INTENTOS = 3;







    public AuthServiceImpl(

            UsuarioRepository repository,

            PasswordEncoder passwordEncoder,

            JwtService jwtService,

            BlockchainService blockchainService

    ){

        this.repository = repository;

        this.passwordEncoder = passwordEncoder;

        this.jwtService = jwtService;

        this.blockchainService = blockchainService;

    }









    /*
     * =====================================================
     *
     * LOGIN
     *
     * Seguridad:
     *
     * - usuario activo
     * - bloqueo
     * - 3 intentos
     * - cambio obligatorio contraseña
     *
     * =====================================================
     */



    @Override
    public LoginResponse login(
            LoginRequest request
    ){



        Usuario usuario = repository.findByUsername(

                request.username()

        )

        .orElseThrow(() ->

                new BusinessException(

                        "Usuario o contraseña incorrectos."

                )

        );







        /*
         * Usuario deshabilitado
         */


        if(!usuario.getActivo()){


            throw new BusinessException(

                    "El usuario está deshabilitado."

            );


        }








        /*
         * Usuario bloqueado por intentos
         */


        if(Boolean.TRUE.equals(
                usuario.getBloqueado()
        )){


            throw new BusinessException(

                    "Usuario bloqueado. Contacte al administrador."

            );


        }








        /*
         * Validación contraseña
         */


        if(!passwordEncoder.matches(

                request.password(),

                usuario.getPassword()

        )){



            registrarIntentoFallido(usuario);



            throw new BusinessException(

                    "Usuario o contraseña incorrectos."

            );


        }








        /*
         * Login correcto
         *
         * limpiamos seguridad
         */


        usuario.setIntentosFallidos(0);


        usuario.setBloqueado(false);


        usuario.setFechaBloqueo(null);


        usuario.setUltimoLogin(

                LocalDateTime.now()

        );



        repository.save(usuario);

        blockchainService.registrarBloque(
                usuario.getUsername(),
                "LOGIN_EXITOSO",
                "Ingreso correcto al sistema.",
                "USUARIO",
                usuario.getId().toString(),
                usuario
        );







        /*
         * Primer ingreso
         *
         * obliga cambio password
         */


        if(Boolean.TRUE.equals(

                usuario.getCambioPasswordPendiente()

        )){


            return new LoginResponse(

                    usuario.getId(),

                    usuario.getNombre(),

                    usuario.getUsername(),

                    usuario.getRol(),

                    null,

                    true

            );


        }







        String token = jwtService.generarToken(

                usuario.getUsername(),

                usuario.getRol()

        );






        return new LoginResponse(

                usuario.getId(),

                usuario.getNombre(),

                usuario.getUsername(),

                usuario.getRol(),

                token,

                false

        );



    }
    /*
     * =====================================================
     *
     * REGISTRAR INTENTO FALLIDO
     *
     * Después de 3 intentos:
     *
     * usuario bloqueado
     *
     * =====================================================
     */


    private void registrarIntentoFallido(

            Usuario usuario

    ){



        Integer intentos = usuario.getIntentosFallidos();



        if(intentos == null){

            intentos = 0;

        }



        intentos++;




        usuario.setIntentosFallidos(

                intentos

        );







        if(intentos >= MAX_INTENTOS){



            usuario.setBloqueado(true);



            usuario.setFechaBloqueo(

                    LocalDateTime.now()

            );

            blockchainService.registrarBloque(
                    usuario.getUsername(),
                    "BLOQUEO_USUARIO",
                    "Usuario bloqueado por superar intentos fallidos.",
                    "USUARIO",
                    usuario.getId().toString(),
                    usuario
            );


        }






        repository.save(usuario);

        if(intentos < MAX_INTENTOS){
            blockchainService.registrarBloque(
                    usuario.getUsername(),
                    "LOGIN_FALLIDO",
                    "Intento fallido " + intentos + "/" + MAX_INTENTOS,
                    "USUARIO",
                    usuario.getId().toString(),
                    usuario
            );
        }



    }









    /*
     * =====================================================
     *
     * CAMBIAR PASSWORD
     *
     * Primer ingreso ROOT
     * o cambio obligatorio
     *
     * =====================================================
     */


    @Override
    public void cambiarPassword(

            CambiarPasswordRequest request

    ){



        Usuario usuario = repository.findByUsername(

                request.username()

        )

        .orElseThrow(() ->


                new BusinessException(

                        "Usuario no encontrado."

                )


        );








        /*
         * Validar contraseña actual
         */


        if(!passwordEncoder.matches(

                request.passwordActual(),

                usuario.getPassword()

        )){


            throw new BusinessException(

                    "La contraseña actual es incorrecta."

            );


        }








        /*
         * Evitar misma contraseña
         */


        if(request.passwordNueva()

                .equals(request.passwordActual())){


            throw new BusinessException(

                    "La nueva contraseña debe ser diferente."

            );


        }









        /*
         * Guardar nueva contraseña
         */


        usuario.setPassword(

                passwordEncoder.encode(

                        request.passwordNueva()

                )

        );








        /*
         * Ya no obliga cambio
         */


        usuario.setCambioPasswordPendiente(false);








        /*
         * Seguridad limpia
         */


        usuario.setIntentosFallidos(0);


        usuario.setBloqueado(false);


        usuario.setFechaBloqueo(null);







        usuario.setUltimoCambioPassword(

                LocalDateTime.now()

        );







        repository.save(usuario);



    }





}
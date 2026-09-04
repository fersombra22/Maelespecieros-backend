package com.maelespecieros.backend.services.impl;


import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.request.UsuarioRequest;
import com.maelespecieros.backend.dto.response.UsuarioResponse;

import com.maelespecieros.backend.entities.Rol;
import com.maelespecieros.backend.entities.Usuario;

import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.DuplicateResourceException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;

import com.maelespecieros.backend.repositories.UsuarioRepository;

import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.UsuarioService;



@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {



    private final UsuarioRepository repository;


    private final PasswordEncoder passwordEncoder;

    private final BlockchainService blockchainService;





    public UsuarioServiceImpl(

            UsuarioRepository repository,

            PasswordEncoder passwordEncoder,

            BlockchainService blockchainService

    ){

        this.repository = repository;

        this.passwordEncoder = passwordEncoder;

        this.blockchainService = blockchainService;

    }








    /*
     * ===============================================
     *
     * CREAR USUARIO
     *
     * ===============================================
     */


    @Override
    public UsuarioResponse crear(
            UsuarioRequest request
    ){



        if(repository.existsByUsername(
                request.username()
        )){


            throw new DuplicateResourceException(
                    "Ya existe un usuario con ese nombre."
            );


        }






        Usuario usuario = new Usuario();


        usuario.setNombre(
                request.nombre()
        );


        usuario.setUsername(
                request.username()
        );


        usuario.setPassword(
                passwordEncoder.encode(
                        request.password()
                )
        );


        usuario.setRol(
                request.rol()
        );


        usuario.setActivo(true);


        usuario.setCambioPasswordPendiente(true);


        usuario.setIntentosFallidos(0);


        usuario.setBloqueado(false);



        Usuario guardado = repository.save(usuario);




        blockchainService.registrarBloque(
                obtenerUsuarioActual(),
                "CREAR USUARIO",
                guardado.getUsername() + " - " + guardado.getRol(),
                "USUARIO",
                guardado.getId().toString(),
                guardado
        );



        return convertirResponse(guardado);


    }









    /*
     * ===============================================
     *
     * LISTAR
     *
     * ===============================================
     */


    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable){


        return repository.findAll(pageable)

                .map(this::convertirResponse);


    }









    /*
     * ===============================================
     *
     * BUSCAR POR ID
     *
     * ===============================================
     */


    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(
            Long id
    ){


        Usuario usuario = repository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        )

                );


        return convertirResponse(usuario);


    }









    /*
     * ===============================================
     *
     * ACTUALIZAR USUARIO
     *
     * ===============================================
     */


    @Override
    public UsuarioResponse actualizar(

            Long id,

            UsuarioRequest request

    ){


        Usuario usuario = repository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        )

                );





        if(usuario.getRol() == Rol.SUPER_ADMIN

                &&

           !esSuperAdminActual()){


            throw new BusinessException(

                    "No puede modificar ROOT."

            );


        }






        if(!usuario.getUsername()
                .equals(request.username())

                &&

           repository.existsByUsername(
                   request.username()
           )){


            throw new DuplicateResourceException(

                    "El username ya existe."

            );


        }





        usuario.setNombre(
                request.nombre()
        );


        usuario.setUsername(
                request.username()
        );





        if(request.password()!=null

                &&

           !request.password().isBlank()){


            usuario.setPassword(

                    passwordEncoder.encode(
                            request.password()
                    )

            );


            usuario.setCambioPasswordPendiente(true);


            usuario.setUltimoCambioPassword(
                    LocalDateTime.now()
            );


        }






        if(request.rol()==Rol.SUPER_ADMIN

                &&

           !esSuperAdminActual()){


            throw new BusinessException(

                    "Solo ROOT puede asignar SUPER_ADMIN."

            );


        }



        usuario.setRol(
                request.rol()
        );



        Usuario actualizado =
                repository.save(usuario);




        blockchainService.registrarBloque(
                obtenerUsuarioActual(),
                "ACTUALIZAR USUARIO",
                actualizado.getUsername(),
                "USUARIO",
                actualizado.getId().toString(),
                actualizado
        );



        return convertirResponse(actualizado);



    }
    /*
     * ===============================================
     *
     * DESACTIVAR USUARIO
     *
     * Reglas:
     *
     * - ROOT nunca se desactiva
     * - Auditoría obligatoria
     *
     * ===============================================
     */


    @Override
    public void desactivar(
            Long id
    ){


        Usuario usuario = repository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        )

                );





        if(usuario.getRol() == Rol.SUPER_ADMIN){


            throw new BusinessException(

                    "El usuario ROOT no puede ser desactivado."

            );


        }





        usuario.setActivo(false);



        repository.save(usuario);




        blockchainService.registrarBloque(
                obtenerUsuarioActual(),
                "DESACTIVAR USUARIO",
                usuario.getUsername(),
                "USUARIO",
                usuario.getId().toString(),
                usuario
        );



    }









    /*
     * ===============================================
     *
     * DESBLOQUEAR USUARIO
     *
     * Solo ROOT debe ejecutar esta acción
     *
     * ===============================================
     */


    @Override
    public void desbloquearUsuario(
            Long id
    ){



        Usuario usuario = repository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        )

                );






        if(!esSuperAdminActual()){


            throw new BusinessException(

                    "Solo ROOT puede desbloquear usuarios."

            );


        }






        usuario.setBloqueado(false);


        usuario.setIntentosFallidos(0);


        usuario.setFechaBloqueo(null);




        repository.save(usuario);






        blockchainService.registrarBloque(
                obtenerUsuarioActual(),
                "DESBLOQUEAR USUARIO",
                usuario.getUsername(),
                "USUARIO",
                usuario.getId().toString(),
                usuario
        );



    }









    /*
     * ===============================================
     *
     * CAMBIAR PASSWORD ADMINISTRATIVO
     *
     * ROOT resetea contraseña
     *
     * Obliga nuevo cambio al ingresar
     *
     * ===============================================
     */


    @Override
    public void cambiarPassword(

            Long id,

            String nuevaPassword

    ){



        Usuario usuario = repository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        )

                );






        if(!esSuperAdminActual()){



            throw new BusinessException(

                    "Solo ROOT puede cambiar contraseñas administrativas."

            );


        }







        usuario.setPassword(

                passwordEncoder.encode(
                        nuevaPassword
                )

        );





        usuario.setCambioPasswordPendiente(true);



        usuario.setIntentosFallidos(0);



        usuario.setBloqueado(false);



        usuario.setFechaBloqueo(null);



        usuario.setUltimoCambioPassword(

                LocalDateTime.now()

        );





        repository.save(usuario);






        blockchainService.registrarBloque(
                obtenerUsuarioActual(),
                "RESET PASSWORD",
                usuario.getUsername(),
                "USUARIO",
                usuario.getId().toString(),
                usuario
        );



    }









    /*
     * ===============================================
     *
     * ENTITY -> RESPONSE
     *
     * Nunca devuelve password
     *
     * ===============================================
     */


    private UsuarioResponse convertirResponse(

            Usuario usuario

    ){



        return new UsuarioResponse(

                usuario.getId(),

                usuario.getNombre(),

                usuario.getUsername(),

                usuario.getRol(),

                usuario.getActivo(),

                usuario.getBloqueado(),

                usuario.getIntentosFallidos(),

                usuario.getCambioPasswordPendiente(),

                usuario.getFechaAlta(),

                usuario.getUltimoLogin(),

                usuario.getFechaBloqueo(),

                usuario.getUltimoCambioPassword()

        );


    }









    /*
     * ===============================================
     *
     * USUARIO ACTUAL PARA AUDITORÍA
     *
     * ===============================================
     */


    private String obtenerUsuarioActual(){



        Authentication authentication =

                SecurityContextHolder

                .getContext()

                .getAuthentication();





        if(authentication == null){


            return "SISTEMA";


        }





        return authentication.getName();



    }









    /*
     * ===============================================
     *
     * VALIDAR ROOT
     *
     * Rol interno:
     *
     * SUPER_ADMIN
     *
     * Usuario:
     *
     * root
     *
     * ===============================================
     */


    private boolean esSuperAdminActual(){



        Authentication authentication =

                SecurityContextHolder

                .getContext()

                .getAuthentication();






        if(authentication == null){


            return false;


        }






        return authentication

                .getAuthorities()

                .stream()

                .anyMatch(

                        authority ->

                        authority.getAuthority()

                        .equals(
                                "ROLE_SUPER_ADMIN"
                        )

                );


    }


}
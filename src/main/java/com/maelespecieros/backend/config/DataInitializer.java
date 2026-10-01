package com.maelespecieros.backend.config;


import java.time.LocalDateTime;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import com.maelespecieros.backend.entities.Rol;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.repositories.UsuarioRepository;



@Component
@lombok.extern.slf4j.Slf4j
public class DataInitializer implements CommandLineRunner {



    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.maelespecieros.backend.repositories.CajaRepository cajaRepository;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            com.maelespecieros.backend.repositories.CajaRepository cajaRepository
    ){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.cajaRepository = cajaRepository;
    }







    @Override
    public void run(String... args){



        if(
            usuarioRepository
            .countByRolAndActivoTrue(
                    Rol.SUPER_ADMIN
            ) == 0
        ){



            Usuario root = new Usuario();




            root.setNombre(
                    "Usuario Root"
            );



            root.setUsername(
                    "root"
            );



            root.setPassword(

                    passwordEncoder.encode(
                            "root123"
                    )

            );



            root.setRol(
                    Rol.SUPER_ADMIN
            );



            root.setActivo(true);



            root.setCambioPasswordPendiente(true);



            root.setIntentosFallidos(0);



            root.setBloqueado(false);



            root.setFechaAlta(
                    LocalDateTime.now()
            );



            usuarioRepository.save(root);





            log.info(
                    "================================="
            );

            log.info(
                    "ROOT creado correctamente"
            );

            log.info(
                    "Usuario: root"
            );

            log.info(
                    "Password temporal: root123"
            );

            log.info(
                    "================================="
            );



        }

        // Sellar criptográficamente cajas históricas si no tienen hash
        for (com.maelespecieros.backend.entities.Caja caja : cajaRepository.findAll()) {
            if (caja.getHashIntegridad() == null) {
                caja.firmarIntegridad();
                cajaRepository.save(caja);
            }
        }
    }



}
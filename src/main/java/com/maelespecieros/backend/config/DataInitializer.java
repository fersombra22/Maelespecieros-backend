package com.maelespecieros.backend.config;


import java.time.LocalDateTime;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import com.maelespecieros.backend.entities.Rol;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.repositories.UsuarioRepository;



@Component
public class DataInitializer implements CommandLineRunner {



    private final UsuarioRepository usuarioRepository;


    private final PasswordEncoder passwordEncoder;




    public DataInitializer(

            UsuarioRepository usuarioRepository,

            PasswordEncoder passwordEncoder

    ){

        this.usuarioRepository = usuarioRepository;

        this.passwordEncoder = passwordEncoder;

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





            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ROOT creado correctamente"
            );

            System.out.println(
                    "Usuario: root"
            );

            System.out.println(
                    "Password temporal: root123"
            );

            System.out.println(
                    "================================="
            );



        }



    }



}
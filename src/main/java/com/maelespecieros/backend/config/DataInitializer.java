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
    private final com.maelespecieros.backend.repositories.ProductoRepository productoRepository;
    private final com.maelespecieros.backend.repositories.VentaRepository ventaRepository;
    private final com.maelespecieros.backend.repositories.EgresoCajaRepository egresoCajaRepository;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            com.maelespecieros.backend.repositories.CajaRepository cajaRepository,
            com.maelespecieros.backend.repositories.ProductoRepository productoRepository,
            com.maelespecieros.backend.repositories.VentaRepository ventaRepository,
            com.maelespecieros.backend.repositories.EgresoCajaRepository egresoCajaRepository
    ){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.cajaRepository = cajaRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.egresoCajaRepository = egresoCajaRepository;
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

        // Sellar criptográficamente solo registros históricos que nunca fueron sellados (hash_integridad IS NULL)
        java.util.List<com.maelespecieros.backend.entities.Caja> cajasSinHash = cajaRepository.findByHashIntegridadIsNull();
        for (com.maelespecieros.backend.entities.Caja caja : cajasSinHash) {
            caja.firmarIntegridad();
            cajaRepository.save(caja);
        }

        java.util.List<com.maelespecieros.backend.entities.Producto> productosSinHash = productoRepository.findByHashIntegridadIsNull();
        for (com.maelespecieros.backend.entities.Producto producto : productosSinHash) {
            producto.firmarIntegridad();
            productoRepository.save(producto);
        }

        java.util.List<com.maelespecieros.backend.entities.Venta> ventasSinHash = ventaRepository.findByHashIntegridadIsNull();
        for (com.maelespecieros.backend.entities.Venta venta : ventasSinHash) {
            venta.firmarIntegridad();
            ventaRepository.save(venta);
        }

        java.util.List<com.maelespecieros.backend.entities.EgresoCaja> egresosSinHash = egresoCajaRepository.findByHashIntegridadIsNull();
        for (com.maelespecieros.backend.entities.EgresoCaja egreso : egresosSinHash) {
            egreso.firmarIntegridad();
            egresoCajaRepository.save(egreso);
        }
    }



}
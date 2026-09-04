package com.maelespecieros.backend.repositories;


import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.maelespecieros.backend.entities.Rol;
import com.maelespecieros.backend.entities.Usuario;



@Repository
public interface UsuarioRepository 
        extends JpaRepository<Usuario, Long> {





    /*
     * Login principal.
     */
    Optional<Usuario> findByUsername(
            String username
    );






    /*
     * Verifica existencia
     * al crear usuarios.
     */
    boolean existsByUsername(
            String username
    );






    /*
     * Login seguro.
     *
     * Usuario habilitado
     * y no bloqueado.
     */
    Optional<Usuario> findByUsernameAndActivoTrueAndBloqueadoFalse(
            
            String username
            
    );






    /*
     * Buscar usuarios bloqueados.
     *
     * Uso exclusivo SUPER_ADMIN.
     */
    Optional<Usuario> findByUsernameAndBloqueadoTrue(
            
            String username
            
    );






    /*
     * Cantidad de usuarios activos
     * por rol.
     *
     * Ej:
     * controlar que exista root.
     */
    long countByRolAndActivoTrue(
            
            Rol rol
            
    );






    /*
     * Buscar root del sistema.
     *
     * Útil para validar
     * operaciones críticas.
     */
    Optional<Usuario> findFirstByRolAndActivoTrue(
            
            Rol rol
            
    );






    /*
     * Listado de usuarios activos.
     */
    java.util.List<Usuario> findAllByActivoTrue();






    /*
     * Listado de usuarios bloqueados.
     */
    java.util.List<Usuario> findAllByBloqueadoTrue();






}
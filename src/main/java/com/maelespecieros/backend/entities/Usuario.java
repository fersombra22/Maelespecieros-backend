package com.maelespecieros.backend.entities;


import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "usuarios")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Usuario {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;





    /*
     * Nombre completo del usuario
     */
    @Column(
            nullable = false,
            length = 80
    )
    private String nombre;





    /*
     * Usuario utilizado para login
     */
    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String username;





    /*
     * Password BCrypt
     */
    @Column(
            nullable = false
    )
    private String password;





    /*
     * Nivel de acceso
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Rol rol;





    /*
     * Usuario habilitado
     *
     * Ej:
     * empleado despedido
     */
    @Column(
            nullable = false
    )
    private Boolean activo;





    /*
     * Obliga cambiar contraseña
     *
     * Primer ingreso root
     */
    @Column(
            nullable = false
    )
    private Boolean cambioPasswordPendiente;





    /*
     * Cantidad de intentos fallidos
     */
    @Column(
            nullable = false
    )
    private Integer intentosFallidos;





    /*
     * Bloqueo por seguridad
     */
    @Column(
            nullable = false
    )
    private Boolean bloqueado;





    /*
     * Fecha en la que fue bloqueado
     */
    private LocalDateTime fechaBloqueo;





    /*
     * Último acceso correcto
     */
    private LocalDateTime ultimoLogin;





    /*
     * Última modificación de contraseña
     */
    private LocalDateTime ultimoCambioPassword;





    /*
     * Fecha creación usuario
     */
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaAlta;





    /*
     * Fecha modificación registro
     */
    private LocalDateTime fechaActualizacion;







    @PrePersist
    public void prePersist(){



        fechaAlta =
                LocalDateTime.now();



        fechaActualizacion =
                LocalDateTime.now();




        if(activo == null){

            activo = true;

        }




        if(cambioPasswordPendiente == null){

            cambioPasswordPendiente = true;

        }




        if(intentosFallidos == null){

            intentosFallidos = 0;

        }




        if(bloqueado == null){

            bloqueado = false;

        }



    }







    @PreUpdate
    public void preUpdate(){


        fechaActualizacion =
                LocalDateTime.now();


    }




}
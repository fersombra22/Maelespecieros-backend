package com.maelespecieros.backend.entities;


import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(
        name = "blockchain_auditoria",
        indexes = {

                @Index(
                        name = "idx_blockchain_fecha",
                        columnList = "fecha"
                ),

                @Index(
                        name = "idx_blockchain_usuario",
                        columnList = "usuario"
                ),

                @Index(
                        name = "idx_blockchain_accion",
                        columnList = "accion"
                ),

                @Index(
                        name = "idx_blockchain_uuid",
                        columnList = "uuid",
                        unique = true
                ),

                @Index(
                        name = "idx_blockchain_hash_actual",
                        columnList = "hash_actual",
                        unique = true
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainAudit {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /*
     * Identificador único del bloque.
     */
    @Column(
            name = "uuid",
            nullable = false,
            unique = true,
            length = 36,
            updatable = false
    )
    private String uuid;




    /*
     * Fecha y hora del evento.
     *
     * Forma parte del hash.
     */
    @Column(
            name = "fecha",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fecha;





    @Column(
            name = "usuario",
            nullable = false,
            length = 100
    )
    private String usuario;





    @Column(
            name = "accion",
            nullable = false,
            length = 100
    )
    private String accion;





    @Column(
            name = "descripcion",
            nullable = false,
            length = 1000
    )
    private String descripcion;

    @Column(
            name = "entidad_tipo",
            length = 100
    )
    private String entidadTipo;

    @Column(
            name = "entidad_id",
            length = 100
    )
    private String entidadId;

    @Column(
            name = "payload_json",
            columnDefinition = "TEXT"
    )
    private String payloadJson;






    /*
     * Hash del bloque anterior.
     */
    @Column(
            name = "hash_anterior",
            nullable = false,
            length = 64
    )
    private String hashAnterior;





    /*
     * Hash propio del bloque.
     */
    @Column(
            name = "hash_actual",
            nullable = false,
            unique = true,
            length = 64
    )
    private String hashActual;





    /*
     * Version blockchain.
     */
    @Column(
            name = "version",
            nullable = false,
            length = 20
    )
    private String version;





    /*
     * Algoritmo utilizado.
     */
    @Column(
            name = "algoritmo",
            nullable = false,
            length = 30
    )
    private String algoritmo;





    /*
     * Numero aleatorio.
     */
    @Column(
            name = "nonce",
            nullable = false
    )
    private Long nonce;





    /*
     * Firma HMAC.
     */
    @Column(
            name = "firma_hmac",
            nullable = false,
            length = 64
    )
    private String firmaHmac;







    @PrePersist
    private void prepararBloque(){



        if(uuid == null || uuid.isBlank()){

            uuid =
                    UUID.randomUUID()
                    .toString();

        }




        if(fecha == null){

            fecha =
                    LocalDateTime.now();

        }




        if(version == null || version.isBlank()){

            version =
                    "1.0";

        }




        if(algoritmo == null || algoritmo.isBlank()){

            algoritmo =
                    "SHA-256";

        }




        if(nonce == null){

            nonce =
                UUID.randomUUID()
                .getMostSignificantBits();

        }



    }



}
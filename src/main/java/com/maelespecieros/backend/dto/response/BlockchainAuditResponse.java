package com.maelespecieros.backend.dto.response;


import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainAuditResponse {



    /*
     * Identificador interno.
     */
    private Long id;





    /*
     * Identificador único del bloque.
     */
    private String uuid;





    /*
     * Fecha y hora del evento.
     */
    private LocalDateTime fecha;





    /*
     * Usuario que ejecutó la acción.
     */
    private String usuario;





    /*
     * Operación realizada.
     */
    private String accion;





    /*
     * Información detallada
     * del evento.
     */
    private String descripcion;





    /*
     * Hash del bloque anterior.
     */
    private String hashAnterior;





    /*
     * Hash generado del bloque actual.
     */
    private String hashActual;





    /*
     * Versión de blockchain.
     */
    private String version;





    /*
     * Algoritmo utilizado.
     */
    private String algoritmo;





    /*
     * Valor aleatorio utilizado
     * para reforzar unicidad.
     */
    private Long nonce;





    /*
     * Firma HMAC del bloque.
     *
     * Solo debe visualizarse
     * por SUPER_ADMIN.
     */
    private String firmaHmac;



}
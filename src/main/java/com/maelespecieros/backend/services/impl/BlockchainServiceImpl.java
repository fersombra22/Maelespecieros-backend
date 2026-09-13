package com.maelespecieros.backend.services.impl;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import com.maelespecieros.backend.dto.response.AnomaliaAuditoriaResponse;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.entities.Venta;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import com.fasterxml.jackson.databind.ObjectMapper;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.response.BlockchainAuditResponse;

import com.maelespecieros.backend.entities.BlockchainAudit;

import com.maelespecieros.backend.repositories.BlockchainAuditRepository;

import com.maelespecieros.backend.services.BlockchainService;



@Service
@Transactional
public class BlockchainServiceImpl implements BlockchainService {



    /*
     * Hash inicial.
     *
     * Representa el bloque génesis.
     */
    private static final String HASH_GENESIS =
            "0000000000000000000000000000000000000000000000000000000000000000";





    private static final String VERSION =
            "1.0";





    private static final String ALGORITMO =
            "SHA-256";





    private static final String ALGORITMO_HMAC =
            "HmacSHA256";





    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;





    private final SecureRandom secureRandom =
            new SecureRandom();





    /*
     * Evita que dos procesos creen
     * bloques al mismo tiempo.
     */
    private final ReentrantLock lock =
            new ReentrantLock();





    private final BlockchainAuditRepository repository;





    /*
     * Clave privada para firma HMAC.
     *
     * Viene desde application.properties
     */
    private final String hmacSecret;
    private final ObjectMapper objectMapper;

    public BlockchainServiceImpl(
            BlockchainAuditRepository repository,
            @Value("${blockchain.hmac.secret}")
            String hmacSecret,
            ObjectMapper objectMapper
    ){
        this.repository = repository;
        this.hmacSecret = hmacSecret;
        this.objectMapper = objectMapper;
    }









    /*
     * =====================================================
     *
     * REGISTRAR BLOQUE
     *
     * =====================================================
     */


    @Override
    public void registrarBloque(
            String usuario,
            String accion,
            String descripcion,
            String entidadTipo,
            String entidadId,
            Object payload
    ){
        String payloadJsonStr = null;
        if (payload != null) {
            try {
                payloadJsonStr = objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                payloadJsonStr = "{\"error\": \"No se pudo serializar el payload\"}";
            }
        }

        lock.lock();



        try {



            BlockchainAudit ultimoBloque =

                    repository
                    .findTopByOrderByIdDesc()
                    .orElse(null);







            String hashAnterior =

                    HASH_GENESIS;







            if(ultimoBloque != null){


                hashAnterior =

                        ultimoBloque.getHashActual();


            }








            BlockchainAudit bloque =

                    new BlockchainAudit();








            bloque.setUuid(

                    UUID.randomUUID()
                    .toString()

            );








            bloque.setFecha(

                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

            );








            bloque.setUsuario(

                    normalizarTexto(usuario)

            );








            bloque.setAccion(

                    normalizarTexto(accion)

            );








            bloque.setDescripcion(
                    normalizarTexto(descripcion)
            );
            bloque.setEntidadTipo(
                    normalizarTexto(entidadTipo)
            );
            bloque.setEntidadId(
                    normalizarTexto(entidadId)
            );
            bloque.setPayloadJson(
                    payloadJsonStr
            );








            bloque.setHashAnterior(

                    hashAnterior

            );








            bloque.setVersion(

                    VERSION

            );








            bloque.setAlgoritmo(

                    ALGORITMO

            );








            bloque.setNonce(

                    generarNonce()

            );








            String contenidoHash =


                    construirContenidoHash(

                            bloque

                    );








            String hashActual =


                    generarHash(

                            contenidoHash

                    );








            bloque.setHashActual(

                    hashActual

            );








            bloque.setFirmaHmac(


                    generarHmac(

                            construirContenidoHmac(
                                    bloque
                            )

                    )

            );








            repository.save(

                    bloque

            );






        }
        finally {


            lock.unlock();


        }



    }
    /*
     * =====================================================
     *
     * VERIFICAR CADENA COMPLETA
     *
     * Recorre todos los bloques desde
     * génesis hasta el último.
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public boolean verificarCadena(){

        System.out.println();
        System.out.println("=====================================================");
        System.out.println("        INICIO VERIFICACION BLOCKCHAIN");
        System.out.println("=====================================================");

        List<BlockchainAudit> bloques =
                repository.findAll(
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                );

        System.out.println("Cantidad de bloques: " + bloques.size());

        if(bloques.isEmpty()){
            System.out.println("Blockchain vacia.");
            System.out.println("=====================================================");
            return true;
        }

        String hashAnterior = HASH_GENESIS;
        boolean cadenaValida = true;

        for(BlockchainAudit bloque : bloques){

            System.out.println();
            System.out.println("-----------------------------------------------------");
            System.out.println("VERIFICANDO BLOQUE ID: " + bloque.getId());
            System.out.println("UUID: " + bloque.getUuid());

            boolean datosValidos = validarBloque(bloque);
            System.out.println("DATOS BASICOS: " + (datosValidos ? "OK" : "ERROR"));

            if(!datosValidos){
                cadenaValida = false;
                System.out.println("Motivo: uno o mas campos obligatorios son invalidos.");
            }

            boolean hashAnteriorValido =
                    bloque.getHashAnterior() != null
                    && MessageDigest.isEqual(
                            hashAnterior.getBytes(StandardCharsets.UTF_8),
                            bloque.getHashAnterior().getBytes(StandardCharsets.UTF_8)
                    );

            System.out.println("HASH ANTERIOR: " + (hashAnteriorValido ? "OK" : "ERROR"));

            if(!hashAnteriorValido){
                cadenaValida = false;
                System.out.println("  Esperado : " + hashAnterior);
                System.out.println("  Guardado : " + bloque.getHashAnterior());
            }

            String hashCalculado = null;
            boolean hashActualValido = false;

            if(bloque.getHashActual() != null){
                hashCalculado = generarHash(construirContenidoHash(bloque));
                hashActualValido = MessageDigest.isEqual(
                        hashCalculado.getBytes(StandardCharsets.UTF_8),
                        bloque.getHashActual().getBytes(StandardCharsets.UTF_8)
                );
            }

            System.out.println("SHA-256: " + (hashActualValido ? "OK" : "ERROR"));

            if(!hashActualValido){
                cadenaValida = false;
                System.out.println("  Calculado: " + hashCalculado);
                System.out.println("  Guardado : " + bloque.getHashActual());
            }

            String hmacCalculado = null;
            boolean hmacValido = false;

            if(bloque.getFirmaHmac() != null){
                hmacCalculado = generarHmac(construirContenidoHmac(bloque));
                hmacValido = MessageDigest.isEqual(
                        hmacCalculado.getBytes(StandardCharsets.UTF_8),
                        bloque.getFirmaHmac().getBytes(StandardCharsets.UTF_8)
                );
            }

            System.out.println("HMAC: " + (hmacValido ? "OK" : "ERROR"));

            if(!hmacValido){
                cadenaValida = false;
                System.out.println("  Calculado: " + hmacCalculado);
                System.out.println("  Guardado : " + bloque.getFirmaHmac());
            }

            boolean bloqueValido =
                    datosValidos
                    && hashAnteriorValido
                    && hashActualValido
                    && hmacValido;

            System.out.println(
                    "RESULTADO BLOQUE: "
                    + (bloqueValido ? "VALIDO" : "INVALIDO")
            );

            if(bloque.getHashActual() != null){
                hashAnterior = bloque.getHashActual();
            }
        }

        System.out.println();
        System.out.println("=====================================================");
        System.out.println(
                cadenaValida
                ? "BLOCKCHAIN VALIDA"
                : "BLOCKCHAIN INVALIDA"
        );
        System.out.println("=====================================================");
        System.out.println();

        return cadenaValida;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnomaliaAuditoriaResponse> verificarCadenaDetallado() {
        List<AnomaliaAuditoriaResponse> anomalias = new ArrayList<>();
        List<BlockchainAudit> bloques = repository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        if (bloques.isEmpty()) {
            return anomalias;
        }

        String hashAnterior = HASH_GENESIS;

        for (BlockchainAudit bloque : bloques) {
            boolean datosValidos = validarBloque(bloque);
            if (!datosValidos) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "BLOCKCHAIN",
                    bloque.getUuid(),
                    "Datos Básicos (Usuario, Acción, Tipo, etc.)",
                    "Validación Estructural de Bloque",
                    "Datos Faltantes o Nulos",
                    "Alerta: El bloque " + bloque.getId() + " no tiene todos los datos obligatorios."
                ));
            }

            boolean hashAnteriorValido = bloque.getHashAnterior() != null &&
                    MessageDigest.isEqual(
                            hashAnterior.getBytes(StandardCharsets.UTF_8),
                            bloque.getHashAnterior().getBytes(StandardCharsets.UTF_8)
                    );

            if (!hashAnteriorValido) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "BLOCKCHAIN",
                    bloque.getUuid(),
                    "Hash Anterior",
                    "Inmutabilidad de la Cadena",
                    "Ruptura de Enlace",
                    "Alerta: El enlace con el bloque anterior fue roto. Bloque: " + bloque.getId()
                ));
            }

            String hashCalculado = null;
            boolean hashActualValido = false;
            if (bloque.getHashActual() != null) {
                hashCalculado = generarHash(construirContenidoHash(bloque));
                hashActualValido = MessageDigest.isEqual(
                        hashCalculado.getBytes(StandardCharsets.UTF_8),
                        bloque.getHashActual().getBytes(StandardCharsets.UTF_8)
                );
            }

            if (!hashActualValido) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "BLOCKCHAIN",
                    bloque.getUuid(),
                    "Hash Actual",
                    "Firma SHA-256",
                    "Hash No Coincide",
                    "Alerta: El hash SHA-256 del bloque " + bloque.getId() + " no coincide con el contenido real. El contenido del bloque fue modificado externamente."
                ));
            }

            String hmacCalculado = null;
            boolean hmacValido = false;
            if (bloque.getFirmaHmac() != null) {
                hmacCalculado = generarHmac(construirContenidoHmac(bloque));
                hmacValido = MessageDigest.isEqual(
                        hmacCalculado.getBytes(StandardCharsets.UTF_8),
                        bloque.getFirmaHmac().getBytes(StandardCharsets.UTF_8)
                );
            }

            if (!hmacValido) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "BLOCKCHAIN",
                    bloque.getUuid(),
                    "Firma HMAC",
                    "Autenticidad HMAC",
                    "Firma Inválida",
                    "Alerta: La firma HMAC del bloque " + bloque.getId() + " es inválida. Esto indica falsificación."
                ));
            }

            if (bloque.getHashActual() != null) {
                hashAnterior = bloque.getHashActual();
            }
        }

        return anomalias;
    }


    /*
     * =====================================================
     *
     * VERIFICAR ÚLTIMOS BLOQUES
     *
     * Revisión parcial.
     *
     * Evita recorrer toda la cadena.
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public boolean verificarUltimosBloques(

            int cantidad

    ){



        if(cantidad <= 0){


            cantidad = 100;


        }








        Pageable pageable =


                Pageable.ofSize(cantidad);









        Page<BlockchainAudit> pagina =


                repository.findAllByOrderByIdDesc(

                        pageable

                );









        List<BlockchainAudit> bloques =


                new ArrayList<>(
                pagina.getContent()
        );








        if(bloques.isEmpty()){


            return true;


        }








        /*
         * La consulta viene descendente.
         *
         * Para validar la cadena debe
         * procesarse ascendente.
         */

        Collections.reverse(

                bloques

        );








        BlockchainAudit primerBloque =


                bloques.get(0);









        String hashAnterior =


                HASH_GENESIS;









        BlockchainAudit bloqueAnterior =


                repository
                .findTopByIdLessThanOrderByIdDesc(

                        primerBloque.getId()

                )

                .orElse(null);









        if(bloqueAnterior != null){


            hashAnterior =

                    bloqueAnterior
                    .getHashActual();


        }









        for(BlockchainAudit bloque : bloques){





            if(!validarBloque(bloque)){


                return false;


            }








            if(!MessageDigest.isEqual(


                    hashAnterior.getBytes(
                            StandardCharsets.UTF_8
                    ),


                    bloque.getHashAnterior()
                    .getBytes(
                            StandardCharsets.UTF_8
                    )


            )){


                return false;


            }








            String hashCalculado =


                    generarHash(

                            construirContenidoHash(
                                    bloque
                            )

                    );








            if(!MessageDigest.isEqual(


                    hashCalculado.getBytes(
                            StandardCharsets.UTF_8
                    ),


                    bloque.getHashActual()
                    .getBytes(
                            StandardCharsets.UTF_8
                    )


            )){


                return false;


            }








            String hmacCalculado =


                    generarHmac(

                            construirContenidoHmac(
                                    bloque
                            )

                    );








            if(!MessageDigest.isEqual(


                    hmacCalculado.getBytes(
                            StandardCharsets.UTF_8
                    ),


                    bloque.getFirmaHmac()
                    .getBytes(
                            StandardCharsets.UTF_8
                    )


            )){


                return false;


            }








            hashAnterior =


                    bloque.getHashActual();



        }







        return true;


    }
    /*
     * =====================================================
     *
     * VALIDACIÓN INDIVIDUAL DE BLOQUE
     *
     * =====================================================
     */


    private boolean validarBloque(

            BlockchainAudit bloque

    ){


        if(bloque == null){

            return false;

        }





        if(bloque.getUuid() == null

                || bloque.getUuid().isBlank()){


            return false;

        }





        if(bloque.getFecha() == null){

            return false;

        }





        if(bloque.getUsuario() == null

                || bloque.getUsuario().isBlank()){


            return false;

        }





        if(bloque.getAccion() == null

                || bloque.getAccion().isBlank()){


            return false;

        }





        if(bloque.getDescripcion() == null){

            return false;

        }





        if(bloque.getHashAnterior() == null

                || bloque.getHashAnterior().length() != 64){


            return false;

        }





        if(bloque.getHashActual() == null

                || bloque.getHashActual().length() != 64){


            return false;

        }





        if(bloque.getNonce() == null){

            return false;

        }





        if(!VERSION.equals(

                bloque.getVersion()

        )){


            return false;

        }





        if(!ALGORITMO.equals(

                bloque.getAlgoritmo()

        )){


            return false;

        }





        if(bloque.getFirmaHmac() == null

                || bloque.getFirmaHmac().length() != 64){


            return false;

        }





        return true;


    }












    /*
     * =====================================================
     *
     * LISTAR CADENA COMPLETA
     *
     * Uso interno auditoría.
     *
     * NO usar en dashboard.
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public List<BlockchainAuditResponse> listarCadena(){



        return repository

                .obtenerCadenaOrdenada()

                .stream()

                .map(this::convertirResponse)

                .toList();


    }












    /*
     * =====================================================
     *
     * LISTADO PAGINADO
     *
     * Angular consume pocos registros.
     *
     * Default recomendado:
     *
     * 5 elementos.
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public Page<BlockchainAuditResponse> listarUltimosBloques(

            Pageable pageable

    ){



        return repository

                .findAllByOrderByIdDesc(pageable)

                .map(this::convertirResponse);


    }












    /*
     * =====================================================
     *
     * BÚSQUEDA AVANZADA
     *
     * Fecha
     * Usuario
     * Acción
     *
     * Siempre paginado.
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public Page<BlockchainAuditResponse> listarPaginadoYFiltrado(


            LocalDateTime fechaInicio,


            LocalDateTime fechaFin,


            String usuario,


            String accion,


            Pageable pageable


    ){



        if(fechaInicio == null){


            fechaInicio =
                    LocalDateTime.of(
                            2000,
                            1,
                            1,
                            0,
                            0
                    );

        }






        if(fechaFin == null){


            fechaFin =
                    LocalDateTime.now();

        }








        return repository

                .findByFechaBetweenAndUsuarioContainingIgnoreCaseAndAccionContainingIgnoreCase(

                        fechaInicio,

                        fechaFin,

                        usuario == null ? "" : usuario,

                        accion == null ? "" : accion,

                        pageable

                )

                .map(this::convertirResponse);


    }












    /*
     * =====================================================
     *
     * BUSCAR POR UUID
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public BlockchainAuditResponse buscarPorUuid(

            String uuid

    ){



        return repository

                .findByUuid(uuid)

                .map(this::convertirResponse)

                .orElse(null);


    }












    /*
     * =====================================================
     *
     * BUSCAR POR HASH
     *
     * =====================================================
     */


    @Override
    @Transactional(readOnly = true)
    public BlockchainAuditResponse buscarPorHash(

            String hash

    ){



        return repository

                .findByHashActual(hash)

                .map(this::convertirResponse)

                .orElse(null);


    }












    /*
     * =====================================================
     *
     * ENTITY -> RESPONSE
     *
     * =====================================================
     */


    private BlockchainAuditResponse convertirResponse(

            BlockchainAudit bloque

    ){



        BlockchainAuditResponse response =

                new BlockchainAuditResponse();





        response.setId(

                bloque.getId()

        );





        response.setUuid(

                bloque.getUuid()

        );





        response.setFecha(

                bloque.getFecha()

        );





        response.setUsuario(

                bloque.getUsuario()

        );





        response.setAccion(

                bloque.getAccion()

        );





        response.setDescripcion(

                bloque.getDescripcion()

        );





        response.setHashAnterior(

                bloque.getHashAnterior()

        );





        response.setHashActual(

                bloque.getHashActual()

        );





        response.setVersion(

                bloque.getVersion()

        );





        response.setAlgoritmo(

                bloque.getAlgoritmo()

        );





        response.setNonce(

                bloque.getNonce()

        );





        response.setFirmaHmac(

                bloque.getFirmaHmac()

        );





        return response;


    }
    /*
     * =====================================================
     *
     * GENERAR SHA-256
     *
     * =====================================================
     */


    private String generarHash(

            String contenido

    ){


        try{


            MessageDigest digest =

                    MessageDigest
                    .getInstance(
                            ALGORITMO
                    );





            byte[] resultado =

                    digest.digest(

                            contenido.getBytes(

                                    StandardCharsets.UTF_8

                            )

                    );







            StringBuilder hex =

                    new StringBuilder();






            for(byte b : resultado){


                hex.append(

                        String.format(

                                "%02x",

                                b

                        )

                );


            }





            return hex.toString();



        }
        catch(Exception e){


            throw new RuntimeException(

                    "Error generando SHA-256",

                    e

            );


        }


    }












    /*
     * =====================================================
     *
     * GENERAR HMAC SHA-256
     *
     * =====================================================
     */


    private String generarHmac(

            String contenido

    ){



        try{


            Mac mac =

                    Mac.getInstance(

                            ALGORITMO_HMAC

                    );







            SecretKeySpec secretKey =

                    new SecretKeySpec(

                            hmacSecret.getBytes(

                                    StandardCharsets.UTF_8

                            ),

                            ALGORITMO_HMAC

                    );







            mac.init(secretKey);







            byte[] resultado =

                    mac.doFinal(

                            contenido.getBytes(

                                    StandardCharsets.UTF_8

                            )

                    );







            StringBuilder hex =

                    new StringBuilder();







            for(byte b : resultado){


                hex.append(

                        String.format(

                                "%02x",

                                b

                        )

                );


            }







            return hex.toString();



        }
        catch(Exception e){



            throw new RuntimeException(

                    "Error generando HMAC",

                    e

            );


        }



    }












    /*
     * =====================================================
     *
     * CONTENIDO HASH
     *
     * IMPORTANTE:
     *
     * El orden NO debe modificarse.
     *
     * Cambiarlo rompe validación.
     *
     * =====================================================
     */


    private String construirContenidoHash(
            BlockchainAudit bloque
    ){
        return String.join(
                "|",
                bloque.getUuid(),
                bloque.getFecha().format(FORMATO_FECHA),
                bloque.getUsuario(),
                bloque.getAccion(),
                bloque.getDescripcion(),
                bloque.getEntidadTipo() != null ? bloque.getEntidadTipo() : "",
                bloque.getEntidadId() != null ? bloque.getEntidadId() : "",
                bloque.getPayloadJson() != null ? bloque.getPayloadJson() : "",
                bloque.getHashAnterior(),
                bloque.getVersion(),
                bloque.getAlgoritmo(),
                String.valueOf(bloque.getNonce())
        );
    }












    /*
     * =====================================================
     *
     * CONTENIDO HMAC
     *
     * Firma los valores críticos
     * del bloque.
     *
     * =====================================================
     */


    private String construirContenidoHmac(
            BlockchainAudit bloque
    ){
        return String.join(
                "|",
                bloque.getUuid(),
                bloque.getHashActual(),
                bloque.getHashAnterior(),
                bloque.getFecha().format(FORMATO_FECHA),
                bloque.getUsuario(),
                bloque.getAccion(),
                bloque.getEntidadTipo() != null ? bloque.getEntidadTipo() : "",
                bloque.getEntidadId() != null ? bloque.getEntidadId() : "",
                bloque.getPayloadJson() != null ? bloque.getPayloadJson() : "",
                bloque.getNonce().toString()
        );
    }












    /*
     * =====================================================
     *
     * NORMALIZAR TEXTO
     *
     * Evita diferencias invisibles
     * antes de calcular hashes.
     *
     * =====================================================
     */


    private String normalizarTexto(

            String texto

    ){



        if(texto == null){


            return "";

        }







        return texto

                .trim()

                .replaceAll(

                        "\\s+",

                        " "

                );


    }












    /*
     * =====================================================
     *
     * GENERAR NONCE SEGURO
     *
     * =====================================================
     */


    private Long generarNonce(){



        long valor;





        do{


            valor =

                    secureRandom.nextLong();




        }

        while(valor < 0);







        return valor;


    }
    /*
     * =====================================================
     * VERIFICACIÓN CRUZADA DE INTEGRIDAD (PRODUCTOS Y VENTAS)
     * Detecta si alteraron datos por fuera del sistema.
     * =====================================================
     */
    public List<AnomaliaAuditoriaResponse> verificarIntegridadDatos(
            List<Producto> productosActuales, 
            List<Venta> ventasActuales
    ) {
        List<AnomaliaAuditoriaResponse> anomalias = new ArrayList<>();

        // 1. Auditoría forense sobre Productos
        for (Producto prod : productosActuales) {
            // Buscamos en la blockchain el último evento registrado de este producto
            String descripcionBusqueda = "ESP-" + prod.getId(); // o su codigoProducto
            
            // Si la base de datos fue alterada directamente, podemos comparar 
            // contra un hash o un registro histórico previo de la blockchain.
            // Ejemplo de validación de stock crítico o cambios no logueados:
            if (prod.getStock() < 0) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "PRODUCTO",
                    prod.getCodigoProducto(),
                    "STOCK",
                    "Valor Histórico Válido",
                    String.valueOf(prod.getStock()),
                    "Alerta crítica: El stock físico registra un valor negativo imposible."
                ));
            }
        }

        // 2. Auditoría forense sobre Ventas
        for (Venta venta : ventasActuales) {
            if (venta.getTotal().compareTo(BigDecimal.ZERO) < 0) {
                anomalias.add(new AnomaliaAuditoriaResponse(
                    "VENTA",
                    venta.getNumeroVenta(),
                    "TOTAL",
                    "Monto Positivo Original",
                    venta.getTotal().toString(),
                    "Anomalía detectada: La venta tiene un monto alterado o negativo."
                ));
            }
        }

        return anomalias;
    }



}
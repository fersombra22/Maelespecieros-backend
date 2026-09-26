package com.maelespecieros.backend.services.impl;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.request.DetalleVentaRequest;
import com.maelespecieros.backend.dto.request.VentaRequest;

import com.maelespecieros.backend.dto.response.DetalleVentaResponse;
import com.maelespecieros.backend.dto.response.VentaResponse;
import com.maelespecieros.backend.dto.response.ComparacionVentasResponse;

import com.maelespecieros.backend.entities.DetalleVenta;
import com.maelespecieros.backend.entities.EstadoVenta;
import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.entities.Venta;
import com.maelespecieros.backend.entities.Cliente;

import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;

import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.repositories.ClienteRepository;

import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.CodigoService;
import com.maelespecieros.backend.services.InventoryService;
import com.maelespecieros.backend.services.PrecioService;
import com.maelespecieros.backend.services.VentaService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;



@Service
@Transactional
public class VentaServiceImpl implements VentaService {



    private final VentaRepository ventaRepository;


    private final ProductoRepository productoRepository;


    private final UsuarioRepository usuarioRepository;

    private final ClienteRepository clienteRepository;

    private final CodigoService codigoService;


    private final PrecioService precioService;


    private final InventoryService inventoryService;

    private final BlockchainService blockchainService;





    public VentaServiceImpl(

            VentaRepository ventaRepository,

            ProductoRepository productoRepository,

            UsuarioRepository usuarioRepository,

            ClienteRepository clienteRepository,

            CodigoService codigoService,

            PrecioService precioService,

            InventoryService inventoryService,

            BlockchainService blockchainService

    ){

        this.ventaRepository = ventaRepository;

        this.productoRepository = productoRepository;

        this.usuarioRepository = usuarioRepository;
        
        this.clienteRepository = clienteRepository;

        this.codigoService = codigoService;

        this.precioService = precioService;

        this.inventoryService = inventoryService;

        this.blockchainService = blockchainService;

    }









    @Override
    public VentaResponse crear(

            VentaRequest request

    ){



        Authentication authentication =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication();



        String username =

                authentication.getName();





        Usuario usuario =

                usuarioRepository.findByUsername(username)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        )

                );







        Venta venta = new Venta();

        if (request.clienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            venta.setCliente(cliente);
        }

        venta.setUsuario(usuario);


        venta.setNumeroVenta(
                codigoService.generarNumeroVenta()
        );


        venta.setFormaPago(
                request.formaPago()
        );


        venta.setEstado(
                EstadoVenta.COMPLETADA
        );







        BigDecimal subtotal =

                BigDecimal.ZERO;



        List<DetalleVenta> detalles =

                new ArrayList<>();









        for(DetalleVentaRequest item : request.detalles()){



            Producto producto =

                    productoRepository.findById(
                            item.productoId()
                    )

                    .orElseThrow(() ->

                            new ResourceNotFoundException(
                                    "Producto no encontrado."
                            )

                    );






            if(!producto.getActivo()){


                throw new BusinessException(

                        "El producto "
                        + producto.getNombre()
                        + " está inactivo."

                );


            }








            BigDecimal precioUnitario =

                    precioService.calcularPrecio(

                            producto,

                            request.formaPago()

                    );







            BigDecimal subtotalDetalle =

                    precioUnitario.multiply(

                            BigDecimal.valueOf(
                                    item.cantidad()
                            )

                    );







            DetalleVenta detalle =

                    new DetalleVenta();



            detalle.setVenta(venta);


            detalle.setProducto(producto);


            detalle.setCantidad(
                    item.cantidad()
            );


            detalle.setPrecioUnitario(
                    precioUnitario
            );


            detalle.setSubtotal(
                    subtotalDetalle
            );




            detalles.add(detalle);




            subtotal =

                    subtotal.add(
                            subtotalDetalle
                    );








        }









        BigDecimal descuento =

                request.descuento() == null

                ? BigDecimal.ZERO

                : request.descuento();







        if(descuento.compareTo(BigDecimal.ZERO) < 0){

            throw new BusinessException(

                    "El descuento no puede ser negativo."

            );

        }







        BigDecimal total =

                subtotal.subtract(descuento);






        if(total.compareTo(BigDecimal.ZERO) < 0){

            throw new BusinessException(

                    "El total de la venta no puede ser negativo."

            );

        }






        venta.setDetalles(detalles);


        venta.setSubtotal(subtotal);


        venta.setDescuento(descuento);


        venta.setTotal(total);







        Venta guardada = ventaRepository.save(venta);

        // Descontar stock (Requerimiento de trazabilidad)
        for (DetalleVenta detalle : guardada.getDetalles()) {
            inventoryService.descontarStock(
                    detalle.getProducto(), 
                    detalle.getCantidad(), 
                    "Venta " + guardada.getNumeroVenta()
            );
        }

        blockchainService.registrarBloque(
                usuario.getUsername(),
                "CREAR_VENTA",
                "Venta creada: " + guardada.getNumeroVenta(),
                "VENTA",
                guardada.getNumeroVenta(),
                guardada
        );






        return convertirResponse(guardada);


    }



    @Override
    public VentaResponse actualizarFormaPago(

            Long id,

            FormaPago nuevaFormaPago

    ){

        Venta venta =

                ventaRepository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Venta no encontrada."
                        )

                );



        if(venta.getEstado() == EstadoVenta.ANULADA){

            throw new BusinessException(

                    "No se puede modificar una venta anulada."

            );

        }



        venta.setFormaPago(nuevaFormaPago);



        BigDecimal subtotal =

                BigDecimal.ZERO;



        for(DetalleVenta detalle : venta.getDetalles()){

            BigDecimal precioUnitario =

                    precioService.calcularPrecio(

                            detalle.getProducto(),

                            nuevaFormaPago

                    );



            BigDecimal subtotalDetalle =

                    precioUnitario.multiply(

                            BigDecimal.valueOf(
                                    detalle.getCantidad()
                            )

                    );



            detalle.setPrecioUnitario(precioUnitario);

            detalle.setSubtotal(subtotalDetalle);



            subtotal =

                    subtotal.add(
                            subtotalDetalle
                    );

        }



        BigDecimal descuento =

                venta.getDescuento() != null

                ? venta.getDescuento()

                : BigDecimal.ZERO;



        BigDecimal total =

                subtotal.subtract(descuento);



        if(total.compareTo(BigDecimal.ZERO) < 0){

            throw new BusinessException(

                    "El total de la venta no puede ser negativo."

            );

        }



        venta.setSubtotal(subtotal);

        venta.setTotal(total);



        Venta guardada =

                ventaRepository.save(venta);



        blockchainService.registrarBloque(
                guardada.getUsuario().getUsername(),
                "ACTUALIZAR_VENTA",
                "Forma de pago actualizada a " + nuevaFormaPago + " en venta: " + guardada.getNumeroVenta(),
                "VENTA",
                guardada.getNumeroVenta(),
                guardada
        );



        return convertirResponse(guardada);

    }



    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> listar(Pageable pageable){


        return ventaRepository

                .findAll(pageable)

                .map(this::convertirResponse);


    }









    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(

            Long id

    ){


        Venta venta =

                ventaRepository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Venta no encontrada."
                        )

                );



        return convertirResponse(venta);


    }









    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorNumero(

            String numeroVenta

    ){


        Venta venta =

                ventaRepository.findByNumeroVenta(numeroVenta)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Venta no encontrada."
                        )

                );



        return convertirResponse(venta);


    }









    @Override
    public void anular(

            Long id

    ){



        Venta venta =

                ventaRepository.findById(id)

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Venta no encontrada."
                        )

                );







        if(venta.getEstado() == EstadoVenta.ANULADA){


            throw new BusinessException(

                    "La venta ya está anulada."

            );


        }








        /*
         * Devolver stock
         */

        venta.getDetalles().forEach(detalle -> {
            inventoryService.ingresarStock(
                    detalle.getProducto(), 
                    detalle.getCantidad(), 
                    "Anulación de venta " + venta.getNumeroVenta()
            );
        });








        venta.setEstado(

                EstadoVenta.ANULADA

        );





        ventaRepository.save(venta);








        blockchainService.registrarBloque(
                venta.getUsuario().getUsername(),
                "ANULAR_VENTA",
                "Venta anulada: " + venta.getNumeroVenta(),
                "VENTA",
                venta.getNumeroVenta(),
                venta
        );



    }
    @Override
    @Transactional(readOnly = true)
    public byte[] generarComprobanteVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);



            Paragraph title = new Paragraph("Comprobante de Venta - Mael Especieros", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Nro Venta: " + venta.getNumeroVenta(), bodyFont));
            document.add(new Paragraph("Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), bodyFont));
            String nombreCliente = venta.getCliente() != null ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() : "Consumidor Final";
            document.add(new Paragraph("Cliente: " + nombreCliente, bodyFont));
            document.add(new Paragraph("Atendido por: " + venta.getUsuario().getUsername(), bodyFont));
            document.add(new Paragraph("Forma de pago: " + venta.getFormaPago(), bodyFont));
            document.add(new Paragraph("Estado: " + venta.getEstado(), bodyFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.5f, 2f, 2.5f});

            table.addCell(new PdfPCell(new Phrase("Producto", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Cant.", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Precio U.", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Subtotal", headerFont)));

            for (DetalleVenta detalle : venta.getDetalles()) {
                table.addCell(new Phrase(detalle.getProducto().getNombre(), bodyFont));
                table.addCell(new Phrase(String.valueOf(detalle.getCantidad()), bodyFont));
                table.addCell(new Phrase("$" + detalle.getPrecioUnitario().toString(), bodyFont));
                table.addCell(new Phrase("$" + detalle.getSubtotal().toString(), bodyFont));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            Paragraph subtotales = new Paragraph("Subtotal: $" + venta.getSubtotal() + "\n" +
                                                 "Descuento: $" + venta.getDescuento() + "\n" +
                                                 "Total: $" + venta.getTotal(), headerFont);
            subtotales.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(subtotales);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el PDF del comprobante");
        }
    }

    private VentaResponse convertirResponse(

            Venta venta

    ){


        List<DetalleVentaResponse> detalles =

                venta.getDetalles()

                .stream()

                .map(this::convertirDetalle)

                .toList();






        return new VentaResponse(


                venta.getId(),


                venta.getNumeroVenta(),


                venta.getFecha(),


                venta.getSubtotal(),


                venta.getDescuento(),


                venta.getTotal(),


                venta.getFormaPago(),


                venta.getEstado(),


                venta.getUsuario().getId(),


                venta.getUsuario().getUsername(),
                
                
                venta.getCliente() != null ? venta.getCliente().getId() : null,


                venta.getCliente() != null ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() : "Consumidor Final",


                detalles


        );


    }









    private DetalleVentaResponse convertirDetalle(

            DetalleVenta detalle

    ){



        return new DetalleVentaResponse(


                detalle.getProducto().getId(),


                detalle.getProducto()

                        .getCodigoProducto(),



                detalle.getProducto()

                        .getNombre(),



                detalle.getCantidad(),



                detalle.getPrecioUnitario(),



                detalle.getSubtotal()



        );


    }

    @Override
    public ComparacionVentasResponse compararVentas(String periodo) {
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        java.time.LocalDateTime inicioActual = ahora;
        java.time.LocalDateTime inicioAnterior = ahora;
        java.time.LocalDateTime finAnterior = ahora;

        switch (periodo.toUpperCase()) {
            case "DIA":
                inicioActual = ahora.withHour(0).withMinute(0).withSecond(0).withNano(0);
                inicioAnterior = inicioActual.minusDays(1);
                finAnterior = inicioActual.minusSeconds(1);
                break;
            case "SEMANA":
                java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
                int diaDeSemana = ahora.get(weekFields.dayOfWeek());
                inicioActual = ahora.minusDays(diaDeSemana - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                inicioAnterior = inicioActual.minusWeeks(1);
                finAnterior = inicioActual.minusSeconds(1);
                break;
            case "MES":
                inicioActual = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                inicioAnterior = inicioActual.minusMonths(1);
                finAnterior = inicioActual.minusSeconds(1);
                break;
            case "ANO":
            case "AÑO":
                inicioActual = ahora.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                inicioAnterior = inicioActual.minusYears(1);
                finAnterior = inicioActual.minusSeconds(1);
                break;
            default:
                throw new BusinessException("Periodo no válido. Use DIA, SEMANA, MES, o ANO.");
        }

        BigDecimal facturadoActual = ventaRepository.obtenerTotalFacturadoEntreFechas(inicioActual, ahora);
        if (facturadoActual == null) facturadoActual = BigDecimal.ZERO;
        
        BigDecimal facturadoAnterior = ventaRepository.obtenerTotalFacturadoEntreFechas(inicioAnterior, finAnterior);
        if (facturadoAnterior == null) facturadoAnterior = BigDecimal.ZERO;

        BigDecimal porcentajeVariacion = BigDecimal.ZERO;
        if (facturadoAnterior.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeVariacion = facturadoActual.subtract(facturadoAnterior)
                    .divide(facturadoAnterior, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else if (facturadoActual.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeVariacion = new BigDecimal("100");
        }

        return new ComparacionVentasResponse(facturadoActual, facturadoAnterior, porcentajeVariacion);
    }

    @Override
    public byte[] generarComparacionPdf(String periodo) {
        ComparacionVentasResponse comparacion = compararVentas(periodo);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font highlightFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

            Paragraph title = new Paragraph("Reporte de Comparación de Ventas", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Período analizado: " + periodo.toUpperCase(), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Total facturado (Período actual): $" + comparacion.actual(), highlightFont));
            document.add(new Paragraph("Total facturado (Período anterior): $" + comparacion.anterior(), highlightFont));
            
            String signo = comparacion.porcentajeVariacion().compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
            document.add(new Paragraph("Variación: " + signo + comparacion.porcentajeVariacion() + "%", highlightFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el PDF de comparación de ventas");
        }
    }
}
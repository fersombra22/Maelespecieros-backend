package com.maelespecieros.backend.services.impl;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.maelespecieros.backend.entities.EstadoCaja;
import com.maelespecieros.backend.entities.EstadoVenta;
import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.entities.Venta;
import com.maelespecieros.backend.entities.Cliente;

import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;

import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.repositories.ClienteRepository;

import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.CodigoService;
import com.maelespecieros.backend.services.EmailService;
import com.maelespecieros.backend.services.InventoryService;
import com.maelespecieros.backend.services.PrecioService;
import com.maelespecieros.backend.services.VentaService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;



@Service
@Transactional
public class VentaServiceImpl implements VentaService {



    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final CajaRepository cajaRepository;
    private final CodigoService codigoService;
    private final PrecioService precioService;
    private final InventoryService inventoryService;
    private final BlockchainService blockchainService;
    private final EmailService emailService;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            CajaRepository cajaRepository,
            CodigoService codigoService,
            PrecioService precioService,
            InventoryService inventoryService,
            BlockchainService blockchainService,
            EmailService emailService
    ){
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.cajaRepository = cajaRepository;
        this.codigoService = codigoService;
        this.precioService = precioService;
        this.inventoryService = inventoryService;
        this.blockchainService = blockchainService;
        this.emailService = emailService;
    }









    @Override
    public VentaResponse crear(
            VentaRequest request
    ){
        if (!cajaRepository.existsByEstado(EstadoCaja.ABIERTA)) {
            throw new BusinessException("No es posible registrar ventas porque no hay ninguna caja abierta. Por favor, realice la apertura de caja antes de operar.");
        }

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

        Map<String, Object> payloadVenta = new LinkedHashMap<>();
        payloadVenta.put("numeroVenta", guardada.getNumeroVenta());
        payloadVenta.put("subtotal", guardada.getSubtotal());
        payloadVenta.put("descuento", guardada.getDescuento());
        payloadVenta.put("total", guardada.getTotal());
        payloadVenta.put("formaPago", guardada.getFormaPago() != null ? guardada.getFormaPago().name() : null);
        payloadVenta.put("estado", guardada.getEstado() != null ? guardada.getEstado().name() : null);
        payloadVenta.put("cajero", usuario.getUsername());
        payloadVenta.put("cliente", guardada.getCliente() != null ? guardada.getCliente().getNombre() : "Consumidor Final");
        payloadVenta.put("cantidadItems", guardada.getDetalles() != null ? guardada.getDetalles().size() : 0);

        blockchainService.registrarBloque(
                usuario.getUsername(),
                "CREAR_VENTA",
                "Venta creada: " + guardada.getNumeroVenta(),
                "VENTA",
                guardada.getNumeroVenta(),
                payloadVenta
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
            Document document = new Document(com.lowagie.text.PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(44, 62, 80));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, new Color(127, 140, 141));
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(40, 40, 40));
            Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(40, 40, 40));
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(39, 174, 96));
            Font disclaimerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(192, 57, 43));
            Font footerSmallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(120, 120, 120));

            // Encabezado
            Paragraph title = new Paragraph("MAEL ESPECIEROS", headerTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("Comprobante de Venta y Control Interno", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);
            document.add(new Paragraph(" "));

            // Metadatos de la Venta
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1f, 1f});

            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBorder(PdfPCell.NO_BORDER);
            cellLeft.addElement(new Paragraph("Nro. Operación: " + venta.getNumeroVenta(), bodyBoldFont));
            cellLeft.addElement(new Paragraph("Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), bodyFont));
            cellLeft.addElement(new Paragraph("Atendido por: " + (venta.getUsuario() != null ? venta.getUsuario().getUsername() : "Cajero"), bodyFont));

            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(PdfPCell.NO_BORDER);
            String nombreCliente = venta.getCliente() != null ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() : "Consumidor Final";
            cellRight.addElement(new Paragraph("Cliente: " + nombreCliente, bodyBoldFont));
            if (venta.getCliente() != null && venta.getCliente().getEmail() != null) {
                cellRight.addElement(new Paragraph("Email: " + venta.getCliente().getEmail(), bodyFont));
            }
            cellRight.addElement(new Paragraph("Forma de pago: " + venta.getFormaPago(), bodyFont));
            cellRight.addElement(new Paragraph("Estado: " + venta.getEstado(), bodyFont));

            metaTable.addCell(cellLeft);
            metaTable.addCell(cellRight);
            document.add(metaTable);
            document.add(new Paragraph(" "));

            // Tabla de Productos
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4.5f, 1.2f, 2f, 2.3f});

            Color headerBg = new Color(52, 73, 94);
            String[] headers = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
            int[] aligns = {Element.ALIGN_LEFT, Element.ALIGN_CENTER, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};

            for (int i = 0; i < headers.length; i++) {
                PdfPCell th = new PdfPCell(new Phrase(headers[i], tableHeaderFont));
                th.setBackgroundColor(headerBg);
                th.setPadding(6);
                th.setHorizontalAlignment(aligns[i]);
                th.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(th);
            }

            boolean alternate = false;
            Color altColor = new Color(245, 247, 250);
            for (DetalleVenta detalle : venta.getDetalles()) {
                Color rowBg = alternate ? altColor : Color.WHITE;

                PdfPCell cellProd = new PdfPCell(new Phrase(detalle.getProducto().getNombre(), bodyFont));
                cellProd.setPadding(5);
                cellProd.setBackgroundColor(rowBg);
                table.addCell(cellProd);

                PdfPCell cellCant = new PdfPCell(new Phrase(String.valueOf(detalle.getCantidad()), bodyFont));
                cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellCant.setPadding(5);
                cellCant.setBackgroundColor(rowBg);
                table.addCell(cellCant);

                PdfPCell cellPrecio = new PdfPCell(new Phrase("$" + detalle.getPrecioUnitario().toString(), bodyFont));
                cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellPrecio.setPadding(5);
                cellPrecio.setBackgroundColor(rowBg);
                table.addCell(cellPrecio);

                PdfPCell cellSub = new PdfPCell(new Phrase("$" + detalle.getSubtotal().toString(), bodyFont));
                cellSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellSub.setPadding(5);
                cellSub.setBackgroundColor(rowBg);
                table.addCell(cellSub);

                alternate = !alternate;
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Resumen de Totales
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(45);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell lblSub = new PdfPCell(new Phrase("Subtotal:", bodyBoldFont));
            lblSub.setBorder(PdfPCell.NO_BORDER);
            PdfPCell valSub = new PdfPCell(new Phrase("$" + venta.getSubtotal(), bodyFont));
            valSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valSub.setBorder(PdfPCell.NO_BORDER);
            totalTable.addCell(lblSub);
            totalTable.addCell(valSub);

            if (venta.getDescuento() != null && venta.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                PdfPCell lblDesc = new PdfPCell(new Phrase("Descuento:", bodyBoldFont));
                lblDesc.setBorder(PdfPCell.NO_BORDER);
                PdfPCell valDesc = new PdfPCell(new Phrase("-$" + venta.getDescuento(), bodyFont));
                valDesc.setHorizontalAlignment(Element.ALIGN_RIGHT);
                valDesc.setBorder(PdfPCell.NO_BORDER);
                totalTable.addCell(lblDesc);
                totalTable.addCell(valDesc);
            }

            PdfPCell lblTot = new PdfPCell(new Phrase("TOTAL:", totalFont));
            lblTot.setBorder(PdfPCell.TOP);
            lblTot.setPaddingTop(5);
            PdfPCell valTot = new PdfPCell(new Phrase("$" + venta.getTotal(), totalFont));
            valTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valTot.setBorder(PdfPCell.TOP);
            valTot.setPaddingTop(5);
            totalTable.addCell(lblTot);
            totalTable.addCell(valTot);

            document.add(totalTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // PIE DE PÁGINA / DISCLAIMER LEGAL OBLIGATORIO
            PdfPTable footerBox = new PdfPTable(1);
            footerBox.setWidthPercentage(100);

            PdfPCell footerCell = new PdfPCell();
            footerCell.setBackgroundColor(new Color(253, 242, 242));
            footerCell.setBorderColor(new Color(235, 180, 180));
            footerCell.setPadding(10);
            footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pDisclaimer = new Paragraph("DOCUMENTO NO VÁLIDO COMO FACTURA", disclaimerFont);
            pDisclaimer.setAlignment(Element.ALIGN_CENTER);
            footerCell.addElement(pDisclaimer);

            Paragraph pLegal = new Paragraph("Comprobante emitido exclusivamente para control comercial interno y entrega de mercadería.\nMael Especieros • Gracias por elegirnos", footerSmallFont);
            pLegal.setAlignment(Element.ALIGN_CENTER);
            footerCell.addElement(pLegal);

            footerBox.addCell(footerCell);
            document.add(footerBox);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el PDF del comprobante: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void enviarComprobantePorEmail(Long id, String destinatario) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));

        String emailFinal = (destinatario != null && !destinatario.isBlank()) ? destinatario.trim() : null;
        if (emailFinal == null && venta.getCliente() != null && venta.getCliente().getEmail() != null) {
            emailFinal = venta.getCliente().getEmail().trim();
        }

        if (emailFinal == null || emailFinal.isBlank()) {
            throw new BusinessException("No se especificó un correo destinatario y el cliente no tiene email registrado.");
        }

        byte[] pdfBytes = generarComprobanteVenta(id);
        emailService.enviarComprobanteVenta(emailFinal, venta.getNumeroVenta(), pdfBytes);
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
                venta.getCliente() != null ? venta.getCliente().getEmail() : null,
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
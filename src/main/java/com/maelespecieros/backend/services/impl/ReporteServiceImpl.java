package com.maelespecieros.backend.services.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.ReporteService;
import com.maelespecieros.backend.services.BlockchainService;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final BlockchainService blockchainService;

    public ReporteServiceImpl(
            ProductoRepository productoRepository,
            VentaRepository ventaRepository,
            BlockchainService blockchainService) {

        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.blockchainService = blockchainService;
    }

    @Override
    public byte[] generarReporteProductos() {

        try {

            Document document = new Document();

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titulo =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Color.BLACK);

            Paragraph encabezado =
                    new Paragraph(
                            "REPORTE DE PRODUCTOS",
                            titulo);

            encabezado.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(encabezado);

            document.add(new Paragraph(" "));

            PdfPTable tabla =
                    new PdfPTable(6);

            tabla.setWidthPercentage(100);

            tabla.setWidths(
                    new float[]{
                            2,
                            4,
                            2,
                            2,
                            2,
                            2
                    });

            agregarCabecera(tabla, "Código");
            agregarCabecera(tabla, "Nombre");
            agregarCabecera(tabla, "Categoría");
            agregarCabecera(tabla, "Stock");
            agregarCabecera(tabla, "Costo");
            agregarCabecera(tabla, "Precio");

            List<Producto> productos =
                    productoRepository.findByActivoTrue();

            for (Producto producto : productos) {

                tabla.addCell(producto.getCodigoProducto());

                tabla.addCell(producto.getNombre());

                tabla.addCell(
                        producto.getCategoria().getNombre());

                tabla.addCell(
                        String.valueOf(
                                producto.getStock()));

                tabla.addCell(
                        producto.getCosto().toString());

                tabla.addCell(
                        producto.getPrecioEfectivo().toString());

            }

            document.add(tabla);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar el PDF.", e);

        }

    }
    @Override
    public byte[] generarReporteVentas() {

        try {

            Document document = new Document();

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titulo =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Color.BLACK);

            Paragraph encabezado =
                    new Paragraph(
                            "REPORTE DE VENTAS",
                            titulo);

            encabezado.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(encabezado);

            document.add(new Paragraph(" "));

            PdfPTable tabla =
                    new PdfPTable(6);

            tabla.setWidthPercentage(100);

            tabla.setWidths(
                    new float[]{
                            3,
                            3,
                            2,
                            2,
                            2,
                            2
                    });

            agregarCabecera(tabla, "Número");
            agregarCabecera(tabla, "Fecha");
            agregarCabecera(tabla, "Forma Pago");
            agregarCabecera(tabla, "Estado");
            agregarCabecera(tabla, "Descuento");
            agregarCabecera(tabla, "Total");

            ventaRepository.findAll().forEach(venta -> {

                tabla.addCell(venta.getNumeroVenta());

                tabla.addCell(
                        venta.getFecha().toString());

                tabla.addCell(
                        venta.getFormaPago().name());

                tabla.addCell(
                        venta.getEstado().name());

                tabla.addCell(
                        venta.getDescuento().toString());

                tabla.addCell(
                        venta.getTotal().toString());

            });

            document.add(tabla);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar reporte de ventas.", e);

        }

    }

    @Override
    public byte[] generarReporteStockBajo() {

        try {

            Document document = new Document();

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titulo =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Color.BLACK);

            Paragraph encabezado =
                    new Paragraph(
                            "REPORTE DE STOCK BAJO",
                            titulo);

            encabezado.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(encabezado);

            document.add(new Paragraph(" "));

            PdfPTable tabla =
                    new PdfPTable(4);

            tabla.setWidthPercentage(100);

            agregarCabecera(tabla, "Código");
            agregarCabecera(tabla, "Producto");
            agregarCabecera(tabla, "Stock");
            agregarCabecera(tabla, "Stock Mínimo");

            productoRepository.findByActivoTrue()
                    .stream()
                    .filter(p -> p.getStock() <= p.getStockMinimo())
                    .forEach(prod -> {

                        tabla.addCell(prod.getCodigoProducto());

                        tabla.addCell(prod.getNombre());

                        tabla.addCell(
                                String.valueOf(prod.getStock()));

                        tabla.addCell(
                                String.valueOf(prod.getStockMinimo()));

                    });

            document.add(tabla);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar reporte de stock.", e);

        }

    }    @Override
    public byte[] generarReporteAuditoria() {

        try {

            Document document = new Document();

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titulo =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Color.BLACK);

            Paragraph encabezado =
                    new Paragraph(
                            "REPORTE DE AUDITORÍA",
                            titulo);

            encabezado.setAlignment(Paragraph.ALIGN_CENTER);

            document.add(encabezado);

            document.add(new Paragraph(" "));

            PdfPTable tabla =
                    new PdfPTable(4);

            tabla.setWidthPercentage(100);

            tabla.setWidths(
                    new float[]{
                            3,
                            3,
                            3,
                            5
                    });

            agregarCabecera(tabla, "Usuario");
            agregarCabecera(tabla, "Acción");
            agregarCabecera(tabla, "Fecha");
            agregarCabecera(tabla, "Detalle");

            blockchainService.listarCadena().forEach(auditoria -> {

                tabla.addCell(auditoria.getUsuario());

                tabla.addCell(auditoria.getAccion());

                tabla.addCell(
                        auditoria.getFecha().toString());

                tabla.addCell(auditoria.getDescripcion());

            });

            document.add(tabla);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar reporte de auditoría.", e);

        }

    }

    private void agregarCabecera(
            PdfPTable tabla,
            String texto) {

        PdfPCell celda =
                new PdfPCell(new Phrase(texto));

        celda.setBackgroundColor(Color.LIGHT_GRAY);

        celda.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);

        tabla.addCell(celda);

    }

}
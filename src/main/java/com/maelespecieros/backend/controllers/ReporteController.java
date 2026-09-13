package com.maelespecieros.backend.controllers;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.maelespecieros.backend.services.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/productos/pdf")
    public ResponseEntity<byte[]> productos() {

        return crearRespuesta(
                service.generarReporteProductos(),
                "productos.pdf", MediaType.APPLICATION_PDF);

    }

    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> productosExcel(
        @org.springframework.beans.factory.annotation.Autowired
        com.maelespecieros.backend.services.ExcelService excelService
    ) {

        return crearRespuesta(
                excelService.exportarProductos(),
                "productos.xlsx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

    }

    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> ventas() {

        return crearRespuesta(
                service.generarReporteVentas(),
                "ventas.pdf", MediaType.APPLICATION_PDF);

    }

    @GetMapping("/stock/pdf")
    public ResponseEntity<byte[]> stock() {

        return crearRespuesta(
                service.generarReporteStockBajo(),
                "stock_bajo.pdf", MediaType.APPLICATION_PDF);

    }

    @GetMapping("/auditoria/pdf")
    public ResponseEntity<byte[]> auditoria() {

        return crearRespuesta(
                service.generarReporteAuditoria(),
                "auditoria.pdf", MediaType.APPLICATION_PDF);

    }

    private ResponseEntity<byte[]> crearRespuesta(
            byte[] archivo,
            String nombreArchivo,
            MediaType mediaType) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(mediaType);

        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(nombreArchivo)
                        .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(archivo);

    }

}
package com.maelespecieros.backend.services;

public interface ReporteService {

    byte[] generarReporteProductos();

    byte[] generarReporteVentas();

    byte[] generarReporteStockBajo();

    byte[] generarReporteAuditoria();

}
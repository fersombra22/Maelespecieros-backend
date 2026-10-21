package com.maelespecieros.backend.services;

public interface ExcelService {

    byte[] exportarProductos();

    byte[] exportarVentas();

    byte[] exportarAuditoria();  

    byte[] exportarStockBajo();   

    void importarProductos(org.springframework.web.multipart.MultipartFile file) throws Exception;
}
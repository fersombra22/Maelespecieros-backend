package com.maelespecieros.backend.services;

public interface EmailService {

    void enviarComprobanteVenta(String destinatario, String numeroVenta, byte[] pdfBytes);

}

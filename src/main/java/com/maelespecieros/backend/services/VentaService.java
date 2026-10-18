package com.maelespecieros.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import com.maelespecieros.backend.dto.request.VentaRequest;
import com.maelespecieros.backend.dto.response.VentaResponse;
import com.maelespecieros.backend.dto.response.ComparacionVentasResponse;
import com.maelespecieros.backend.entities.FormaPago;

public interface VentaService {

    VentaResponse crear(VentaRequest request);

    VentaResponse actualizarFormaPago(Long id, FormaPago nuevaFormaPago);

    byte[] generarComprobanteVenta(Long id);

    Page<VentaResponse> listar(Pageable pageable);

    VentaResponse obtenerPorId(Long id);

    VentaResponse obtenerPorNumero(String numeroVenta);

    void anular(Long id);

    ComparacionVentasResponse compararVentas(String periodo);

    byte[] generarComparacionPdf(String periodo);

}
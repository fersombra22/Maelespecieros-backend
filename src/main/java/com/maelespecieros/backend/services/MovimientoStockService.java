package com.maelespecieros.backend.services;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import com.maelespecieros.backend.dto.request.MovimientoStockRequest;
import com.maelespecieros.backend.dto.response.MovimientoStockResponse;



public interface MovimientoStockService {



    MovimientoStockResponse registrarMovimiento(
            MovimientoStockRequest request);



    Page<MovimientoStockResponse> listar(
            Pageable pageable);



    Page<MovimientoStockResponse> listarPorProducto(
            Long productoId,
            Pageable pageable);



}
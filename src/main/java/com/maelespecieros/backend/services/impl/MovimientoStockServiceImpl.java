package com.maelespecieros.backend.services.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.request.MovimientoStockRequest;
import com.maelespecieros.backend.dto.response.MovimientoStockResponse;

import com.maelespecieros.backend.entities.Producto;

import com.maelespecieros.backend.exceptions.ResourceNotFoundException;

import com.maelespecieros.backend.repositories.MovimientoStockRepository;
import com.maelespecieros.backend.repositories.ProductoRepository;

import com.maelespecieros.backend.services.InventoryService;
import com.maelespecieros.backend.services.MovimientoStockService;



@Service
@Transactional
public class MovimientoStockServiceImpl 
        implements MovimientoStockService {



    private final MovimientoStockRepository movimientoRepository;


    private final ProductoRepository productoRepository;


    private final InventoryService inventoryService;







    public MovimientoStockServiceImpl(

            MovimientoStockRepository movimientoRepository,

            ProductoRepository productoRepository,

            InventoryService inventoryService

    ){

        this.movimientoRepository = movimientoRepository;

        this.productoRepository = productoRepository;

        this.inventoryService = inventoryService;

    }









    @Override
    public MovimientoStockResponse registrarMovimiento(

            MovimientoStockRequest request

    ){



        Producto producto =

                productoRepository.findById(
                        request.productoId()
                )

                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Producto no encontrado."
                        )

                );







        Integer cantidadApi = request.cantidad();
        if (cantidadApi == null || cantidadApi == 0) {
            throw new com.maelespecieros.backend.exceptions.BusinessException("La cantidad no puede ser cero.");
        }

        com.maelespecieros.backend.entities.TipoMovimiento tipo = request.tipoMovimiento();
        Integer cantidadAbs = Math.abs(cantidadApi);
        boolean isAddition = cantidadApi > 0;

        switch (tipo) {
            case ENTRADA:
            case DEVOLUCION:
            case ANULACION_VENTA:
                return inventoryService.ingresarStock(producto, cantidadAbs, request.motivo());
            case VENTA:
                return inventoryService.descontarStock(producto, cantidadAbs, request.motivo());
            case AJUSTE:
                if (isAddition) {
                    return inventoryService.ajustePositivo(producto, cantidadAbs, request.motivo());
                } else {
                    return inventoryService.ajusteNegativo(producto, cantidadAbs, request.motivo());
                }
            default:
                throw new com.maelespecieros.backend.exceptions.BusinessException("Tipo de movimiento no soportado.");
        }

    }









    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoStockResponse> listar(

            Pageable pageable

    ){


        return movimientoRepository

                .findAllByOrderByFechaDesc(pageable)

                .map(this::convertirResponse);


    }









    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoStockResponse> listarPorProducto(

            Long productoId,

            Pageable pageable

    ){


        return movimientoRepository

                .findByProductoIdOrderByFechaDesc(

                        productoId,

                        pageable

                )

                .map(this::convertirResponse);


    }









    private MovimientoStockResponse convertirResponse(

            com.maelespecieros.backend.entities.MovimientoStock movimiento

    ){


        return new MovimientoStockResponse(

                movimiento.getId(),

                movimiento.getProducto()
                        .getCodigoProducto(),

                movimiento.getProducto()
                        .getNombre(),

                movimiento.getTipoMovimiento(),

                movimiento.getCantidad(),

                movimiento.getStockAnterior(),

                movimiento.getStockNuevo(),

                movimiento.getMotivo(),

                movimiento.getFecha()

        );


    }


}
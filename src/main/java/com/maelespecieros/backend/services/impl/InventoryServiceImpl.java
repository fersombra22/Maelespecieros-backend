package com.maelespecieros.backend.services.impl;


import java.time.LocalDateTime;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.response.MovimientoStockResponse;

import com.maelespecieros.backend.entities.MovimientoStock;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.entities.TipoMovimiento;

import com.maelespecieros.backend.exceptions.BusinessException;

import com.maelespecieros.backend.repositories.MovimientoStockRepository;
import com.maelespecieros.backend.repositories.ProductoRepository;

import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.InventoryService;



@Service
@Transactional
public class InventoryServiceImpl 
        implements InventoryService {




    private final ProductoRepository productoRepository;


    private final MovimientoStockRepository movimientoRepository;


    private final BlockchainService blockchainService;







    public InventoryServiceImpl(

            ProductoRepository productoRepository,

            MovimientoStockRepository movimientoRepository,

            BlockchainService blockchainService

    ){


        this.productoRepository = productoRepository;

        this.movimientoRepository = movimientoRepository;

        this.blockchainService = blockchainService;

    }









    private MovimientoStockResponse moverStock(

            Producto producto,

            Integer cantidad,

            TipoMovimiento tipoMovimiento,

            boolean isAddition,

            String motivo

    ){


        if(cantidad == null || cantidad <= 0){


            throw new BusinessException(

                    "La cantidad debe ser mayor a cero."

            );

        }


        Integer stockAnterior =

                producto.getStock();


        Integer stockNuevo;

        if (isAddition) {
            stockNuevo = stockAnterior + cantidad;
        } else {
            if (stockAnterior < cantidad) {
                throw new BusinessException("Stock insuficiente.");
            }
            stockNuevo = stockAnterior - cantidad;
        }










        producto.setStock(stockNuevo);



        productoRepository.save(producto);










        MovimientoStock movimiento =

                new MovimientoStock();




        movimiento.setProducto(producto);


        movimiento.setTipoMovimiento(
                tipoMovimiento
        );


        movimiento.setCantidad(
                cantidad
        );


        movimiento.setStockAnterior(
                stockAnterior
        );


        movimiento.setStockNuevo(
                stockNuevo
        );


        movimiento.setMotivo(
                motivo
        );



        MovimientoStock guardado =

                movimientoRepository.save(movimiento);









        Authentication authentication =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication();





        String usuario =


                authentication != null

                ? authentication.getName()

                : "SISTEMA";









        blockchainService.registrarBloque(
                usuario,
                tipoMovimiento.name(),
                producto.getCodigoProducto() + " STOCK " + stockAnterior + " -> " + stockNuevo + " | " + motivo,
                "MOVIMIENTO_STOCK",
                producto.getCodigoProducto(),
                producto
        );









        return convertirResponse(guardado);



    }









    @Override
    public MovimientoStockResponse ingresarStock(

            Producto producto,

            Integer cantidad,

            String motivo

    ){


        return moverStock(

                producto,

                cantidad,

                TipoMovimiento.ENTRADA,

                true,

                motivo

        );


    }









    @Override
    public MovimientoStockResponse descontarStock(

            Producto producto,

            Integer cantidad,

            String motivo

    ){


        return moverStock(

                producto,

                cantidad,

                TipoMovimiento.VENTA,

                false,

                motivo

        );


    }









    @Override
    public MovimientoStockResponse ajustePositivo(

            Producto producto,

            Integer cantidad,

            String motivo

    ){


        return moverStock(

                producto,

                cantidad,

                TipoMovimiento.AJUSTE,

                true,

                motivo

        );


    }









    @Override
    public MovimientoStockResponse ajusteNegativo(

            Producto producto,

            Integer cantidad,

            String motivo

    ){


        return moverStock(

                producto,

                cantidad,

                TipoMovimiento.AJUSTE,

                false,

                motivo

        );


    }









    private MovimientoStockResponse convertirResponse(

            MovimientoStock movimiento

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
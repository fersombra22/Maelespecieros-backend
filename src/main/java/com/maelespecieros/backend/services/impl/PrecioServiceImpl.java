package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.services.PrecioService;

@Service
public class PrecioServiceImpl implements PrecioService {

    @Override
    public BigDecimal calcularPrecio(
            Producto producto,
            FormaPago formaPago) {

        if (formaPago == FormaPago.EFECTIVO
                || formaPago == FormaPago.TRANSFERENCIA) {

            return producto.getPrecioEfectivo();
        }

        return producto.getPrecioEfectivo()
                .divide(
                        new BigDecimal("0.80"),
                        2,
                        RoundingMode.HALF_UP);

    }

}
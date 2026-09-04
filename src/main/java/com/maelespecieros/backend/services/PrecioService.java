package com.maelespecieros.backend.services;

import java.math.BigDecimal;

import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.Producto;

public interface PrecioService {

    BigDecimal calcularPrecio(
            Producto producto,
            FormaPago formaPago);

}
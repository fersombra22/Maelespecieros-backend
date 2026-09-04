package com.maelespecieros.backend.services;

import com.maelespecieros.backend.entities.Categoria;

public interface CodigoService {

    String generarCodigoProducto(Categoria categoria);

    String generarNumeroVenta();

}
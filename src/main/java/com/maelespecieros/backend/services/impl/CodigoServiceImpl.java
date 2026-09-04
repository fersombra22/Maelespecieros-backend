package com.maelespecieros.backend.services.impl;

import org.springframework.stereotype.Service;

import com.maelespecieros.backend.entities.Categoria;
import com.maelespecieros.backend.services.CodigoService;
import com.maelespecieros.backend.services.NumeradorService;

@Service
public class CodigoServiceImpl implements CodigoService {

    private final NumeradorService numeradorService;

    public CodigoServiceImpl(NumeradorService numeradorService) {
        this.numeradorService = numeradorService;
    }

    @Override
    public String generarCodigoProducto(Categoria categoria) {

        return numeradorService.generarCodigoProducto(
                categoria.getPrefijo());

    }

    @Override
    public String generarNumeroVenta() {

        return numeradorService.generarNumeroVenta();

    }

}
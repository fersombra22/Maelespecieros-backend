package com.maelespecieros.backend.services;


import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maelespecieros.backend.dto.request.ProductoRequest;
import com.maelespecieros.backend.dto.response.ProductoResponse;



public interface ProductoService {



    ProductoResponse crear(
            ProductoRequest request
    );



    Page<ProductoResponse> listar(Pageable pageable);



    ProductoResponse obtenerPorId(
            Long id
    );



    ProductoResponse buscarPorCodigo(
            String codigo
    );



    Page<ProductoResponse> buscarPorNombre(
            String nombre,
            Pageable pageable
    );



    Page<ProductoResponse> listarPorCategoria(
            Long categoriaId,
            Pageable pageable
    );



    ProductoResponse actualizar(
            Long id,
            ProductoRequest request
    );



    void desactivar(
            Long id
    );



    void activar(
            Long id
    );

    void aumentoMasivo(
            List<Long> ids,
            java.math.BigDecimal porcentaje
    );

}
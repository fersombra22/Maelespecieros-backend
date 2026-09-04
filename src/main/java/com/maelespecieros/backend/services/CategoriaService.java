package com.maelespecieros.backend.services;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maelespecieros.backend.dto.request.CategoriaRequest;
import com.maelespecieros.backend.dto.response.CategoriaResponse;



public interface CategoriaService {



    CategoriaResponse crear(
            CategoriaRequest request
    );



    CategoriaResponse actualizar(

            Long id,

            CategoriaRequest request

    );



    Page<CategoriaResponse> listar(

            Pageable pageable

    );



    List<CategoriaResponse> listarTodas();



    CategoriaResponse obtenerPorId(

            Long id

    );



    void desactivar(

            Long id

    );



    void activar(

            Long id

    );


}
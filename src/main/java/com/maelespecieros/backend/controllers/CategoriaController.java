package com.maelespecieros.backend.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.common.Messages;
import com.maelespecieros.backend.dto.request.CategoriaRequest;
import com.maelespecieros.backend.dto.response.CategoriaResponse;
import com.maelespecieros.backend.services.CategoriaService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/categorias")
@Validated
public class CategoriaController {


    private final CategoriaService service;


    public CategoriaController(
            CategoriaService service
    ){

        this.service = service;

    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponse>> crear(

            @Valid
            @RequestBody
            CategoriaRequest request

    ){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                Messages.CREATED,
                                service.crear(request)
                        )
                );

    }

    /*
     * ==================================================
     *
     * LISTADO PAGINADO
     *
     * Producción:
     * Nunca traer toda la tabla.
     *
     * Ejemplo:
     *
     * /api/categorias?page=0&size=5
     *
     * ==================================================
     */


    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoriaResponse>>> listar(

            @RequestParam(defaultValue = "0")
            int page,


            @RequestParam(defaultValue = "5")
            int size

    ){


        Pageable pageable =
                PageRequest.of(

                        page,

                        size,

                        Sort.by(
                                Sort.Direction.ASC,
                                "nombre"
                        )

                );



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.listar(pageable)

                )

        );


    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> obtenerPorId(

            @PathVariable
            Long id

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.FOUND,

                        service.obtenerPorId(id)

                )

        );


    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizar(

            @PathVariable
            Long id,


            @Valid
            @RequestBody
            CategoriaRequest request

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.UPDATED,

                        service.actualizar(id, request)

                )

        );


    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desactivar(

            @PathVariable
            Long id

    ){


        service.desactivar(id);


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.DELETED,

                        null

                )

        );


    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(

            @PathVariable
            Long id

    ){


        service.activar(id);


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.UPDATED,

                        null

                )

        );


    }


}

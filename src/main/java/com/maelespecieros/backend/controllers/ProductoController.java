package com.maelespecieros.backend.controllers;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.common.Messages;
import com.maelespecieros.backend.dto.request.ProductoRequest;
import com.maelespecieros.backend.dto.response.ProductoResponse;
import com.maelespecieros.backend.services.ProductoService;


import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/productos")
@Validated
public class ProductoController {



    private final ProductoService service;



    public ProductoController(
            ProductoService service
    ){

        this.service = service;

    }






    // SOLO ADMIN

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(

            @Valid @RequestBody ProductoRequest request

    ){


        ProductoResponse response = service.crear(request);



        return ResponseEntity.status(HttpStatus.CREATED)

                .body(new ApiResponse<>(

                        true,

                        Messages.CREATED,

                        response

                ));

    }








    // SUPER_ADMIN,ADMIN Y EMPLEADO

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductoResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.listar(PageRequest.of(page, size))

                )

        );

    }








    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(

            @PathVariable Long id

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.FOUND,

                        service.obtenerPorId(id)

                )

        );

    }








    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<ProductoResponse>> buscarPorCodigo(

            @PathVariable String codigo

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.FOUND,

                        service.buscarPorCodigo(codigo)

                )

        );

    }








    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<Page<ProductoResponse>>> buscarPorNombre(

            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.buscarPorNombre(nombre, PageRequest.of(page, size))

                )

        );

    }








    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EMPLEADO')")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<Page<ProductoResponse>>> listarPorCategoria(

            @PathVariable Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.listarPorCategoria(categoriaId, PageRequest.of(page, size))

                )

        );

    }








    // SOLO ADMIN Y SUPER_ADMIN

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(

            @PathVariable Long id,

            @Valid @RequestBody ProductoRequest request

    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.UPDATED,

                        service.actualizar(id, request)

                )

        );

    }








    // SOLO ADMIN Y SUPER ADMIN

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desactivar(

            @PathVariable Long id

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



}
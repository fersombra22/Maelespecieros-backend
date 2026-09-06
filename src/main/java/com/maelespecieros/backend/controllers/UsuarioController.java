package com.maelespecieros.backend.controllers;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;


import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.common.Messages;
import com.maelespecieros.backend.dto.request.UsuarioRequest;
import com.maelespecieros.backend.dto.response.UsuarioResponse;
import com.maelespecieros.backend.services.UsuarioService;


import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {



    private final UsuarioService service;



    public UsuarioController(
            UsuarioService service
    ) {

        this.service = service;

    }







    // SOLO ADMIN CREA USUARIOS

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(

            @Valid @RequestBody UsuarioRequest request

    ) {



        UsuarioResponse response = service.crear(request);



        return ResponseEntity.status(HttpStatus.CREATED)

                .body(new ApiResponse<>(

                        true,

                        Messages.CREATED,

                        response

                ));


    }








    // SOLO ADMIN PUEDEN LISTAR

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.listar(PageRequest.of(page, size))

                )

        );


    }








    // SOLO ADMIN PUEDEN CONSULTAR

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtener(

            @PathVariable Long id

    ) {



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.FOUND,

                        service.obtenerPorId(id)

                )

        );


    }








    // SOLO ADMIN MODIFICA

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(

            @PathVariable Long id,

            @Valid @RequestBody UsuarioRequest request

    ) {



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.UPDATED,

                        service.actualizar(id, request)

                )

        );


    }








    // SOLO ADMIN DESACTIVA

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desactivar(

            @PathVariable Long id

    ) {



        service.desactivar(id);



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.DELETED,

                        null

                )

        );


    }

    // SOLO ROOT DESBLOQUEA
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse<Void>> desbloquear(
            @PathVariable Long id
    ) {

        service.desbloquearUsuario(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Usuario desbloqueado correctamente.",
                        null
                )
        );
    }

}
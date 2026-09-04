package com.maelespecieros.backend.controllers;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.common.Messages;
import com.maelespecieros.backend.dto.request.MovimientoStockRequest;
import com.maelespecieros.backend.dto.response.MovimientoStockResponse;
import com.maelespecieros.backend.services.MovimientoStockService;


import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/movimientos")
@Validated
public class MovimientoStockController {



    private final MovimientoStockService service;



    public MovimientoStockController(MovimientoStockService service) {

        this.service = service;

    }








    @PostMapping
    public ResponseEntity<ApiResponse<MovimientoStockResponse>> registrar(

            @Valid @RequestBody MovimientoStockRequest request) {


        MovimientoStockResponse response =
                service.registrarMovimiento(request);



        return ResponseEntity.status(HttpStatus.CREATED)

                .body(

                        new ApiResponse<>(

                                true,

                                Messages.CREATED,

                                response

                        )

                );

    }









    @GetMapping
    public ResponseEntity<ApiResponse<Page<MovimientoStockResponse>>> listar(

            @RequestParam(defaultValue = "0") int pagina,

            @RequestParam(defaultValue = "5") int cantidad) {



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.listar(

                                PageRequest.of(

                                        pagina,

                                        cantidad

                                )

                        )

                )

        );

    }










    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<Page<MovimientoStockResponse>>> listarPorProducto(

            @PathVariable Long productoId,

            @RequestParam(defaultValue = "0") int pagina,

            @RequestParam(defaultValue = "5") int cantidad) {



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        Messages.LIST,

                        service.listarPorProducto(

                                productoId,

                                PageRequest.of(

                                        pagina,

                                        cantidad

                                )

                        )

                )

        );


    }



}
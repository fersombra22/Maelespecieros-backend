package com.maelespecieros.backend.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import com.maelespecieros.backend.common.ApiResponse;
import com.maelespecieros.backend.dto.request.CambiarPasswordRequest;
import com.maelespecieros.backend.dto.request.LoginRequest;
import com.maelespecieros.backend.dto.response.LoginResponse;
import com.maelespecieros.backend.services.AuthService;


import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {



    private final AuthService service;



    public AuthController(
            AuthService service
    ){

        this.service = service;

    }

    /*
     * LOGIN SISTEMA
     *
     * Control de:
     * - usuario activo
     * - intentos fallidos
     * - bloqueo
     * - cambio obligatorio password
     * - generación JWT
     *
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(


            @Valid
            @RequestBody LoginRequest request


    ){


        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        "Login correcto.",

                        service.login(request)

                )

        );


    }
    /*
     * CAMBIO DE PASSWORD OBLIGATORIO
     *
     * Se utiliza:
     *
     * - primer ingreso root
     * - primer ingreso usuario creado
     * - recuperación por desbloqueo
     *
     */
    @PutMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(


            @Valid
            @RequestBody CambiarPasswordRequest request


    ){


        service.cambiarPassword(request);



        return ResponseEntity.ok(

                new ApiResponse<>(

                        true,

                        "Contraseña actualizada correctamente.",

                        null

                )

        );


    }



}
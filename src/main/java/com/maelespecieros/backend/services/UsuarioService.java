package com.maelespecieros.backend.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maelespecieros.backend.dto.request.UsuarioRequest;
import com.maelespecieros.backend.dto.response.UsuarioResponse;

public interface UsuarioService {

    /*
     * ===============================
     * CRUD
     * ===============================
     */

    UsuarioResponse crear(
            UsuarioRequest request
    );

    Page<UsuarioResponse> listar(Pageable pageable);

    UsuarioResponse obtenerPorId(
            Long id
    );

    UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request
    );

    void desactivar(
            Long id
    );

    /*
     * ===============================
     * SEGURIDAD
     * ===============================
     */

    void desbloquearUsuario(
            Long id
    );

    void cambiarPassword(
            Long id,
            String nuevaPassword
    );

}
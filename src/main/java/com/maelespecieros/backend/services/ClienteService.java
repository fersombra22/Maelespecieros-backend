package com.maelespecieros.backend.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maelespecieros.backend.dto.request.ClienteRequest;
import com.maelespecieros.backend.dto.response.ClienteResponse;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    Page<ClienteResponse> listar(String termino, Boolean activo, Pageable pageable);

    List<ClienteResponse> listarTodosActivos();

    ClienteResponse obtenerPorId(Long id);

    void desactivar(Long id);

    void activar(Long id);

    void eliminar(Long id);
}

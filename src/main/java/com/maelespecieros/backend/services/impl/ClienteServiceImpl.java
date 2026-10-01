package com.maelespecieros.backend.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.request.ClienteRequest;
import com.maelespecieros.backend.dto.response.ClienteResponse;
import com.maelespecieros.backend.entities.Cliente;
import com.maelespecieros.backend.exceptions.DuplicateResourceException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import com.maelespecieros.backend.repositories.ClienteRepository;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.ClienteService;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final BlockchainService blockchainService;

    public ClienteServiceImpl(
            ClienteRepository repository,
            BlockchainService blockchainService
    ) {
        this.repository = repository;
        this.blockchainService = blockchainService;
    }

    private String getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
    }

    @Override
    public ClienteResponse crear(ClienteRequest request) {
        String emailLimpio = request.email().trim().toLowerCase();

        if (repository.existsByEmail(emailLimpio)) {
            throw new DuplicateResourceException("Ya existe un cliente con el email: " + emailLimpio);
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre().trim());
        cliente.setApellido(request.apellido().trim());
        cliente.setEmail(emailLimpio);
        cliente.setTelefono(request.telefono() != null ? request.telefono().trim() : null);
        cliente.setDireccion(request.direccion() != null ? request.direccion().trim() : null);
        cliente.setActivo(request.activo() != null ? request.activo() : true);

        Cliente guardado = repository.save(cliente);

        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "CREAR_CLIENTE",
                "Cliente registrado: " + guardado.getNombre() + " " + guardado.getApellido(),
                "CLIENTE",
                guardado.getId().toString(),
                guardado
        );

        return convertir(guardado);
    }

    @Override
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        String emailLimpio = request.email().trim().toLowerCase();
        if (repository.existsByEmailAndIdNot(emailLimpio, id)) {
            throw new DuplicateResourceException("Ya existe otro cliente con el email: " + emailLimpio);
        }

        cliente.setNombre(request.nombre().trim());
        cliente.setApellido(request.apellido().trim());
        cliente.setEmail(emailLimpio);
        cliente.setTelefono(request.telefono() != null ? request.telefono().trim() : null);
        cliente.setDireccion(request.direccion() != null ? request.direccion().trim() : null);
        if (request.activo() != null) {
            cliente.setActivo(request.activo());
        }

        Cliente actualizado = repository.save(cliente);

        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ACTUALIZAR_CLIENTE",
                "Cliente actualizado: " + actualizado.getNombre() + " " + actualizado.getApellido(),
                "CLIENTE",
                actualizado.getId().toString(),
                actualizado
        );

        return convertir(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String termino, Boolean activo, Pageable pageable) {
        String terminoLimpio = (termino != null && !termino.trim().isEmpty()) ? termino.trim() : null;
        Page<Cliente> pagina = repository.buscarConFiltros(terminoLimpio, activo, pageable);
        return pagina.map(this::convertir);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodosActivos() {
        return repository.findByActivoTrueOrderByApellidoAscNombreAsc()
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return convertir(cliente);
    }

    @Override
    public void desactivar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setActivo(false);
        repository.save(cliente);

        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "DESACTIVAR_CLIENTE",
                "Cliente desactivado: " + cliente.getNombre() + " " + cliente.getApellido(),
                "CLIENTE",
                cliente.getId().toString(),
                cliente
        );
    }

    @Override
    public void activar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setActivo(true);
        repository.save(cliente);

        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ACTIVAR_CLIENTE",
                "Cliente reactivado: " + cliente.getNombre() + " " + cliente.getApellido(),
                "CLIENTE",
                cliente.getId().toString(),
                cliente
        );
    }

    @Override
    public void eliminar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        repository.delete(cliente);

        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ELIMINAR_CLIENTE",
                "Cliente eliminado definitivamente: " + cliente.getNombre() + " " + cliente.getApellido(),
                "CLIENTE",
                id.toString(),
                null
        );
    }

    private ClienteResponse convertir(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                Boolean.TRUE.equals(cliente.getActivo()),
                cliente.getFechaAlta(),
                cliente.getFechaActualizacion()
        );
    }
}

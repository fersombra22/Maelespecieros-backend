package com.maelespecieros.backend.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.request.GastoOperativoRequest;
import com.maelespecieros.backend.dto.response.GastoOperativoResponse;
import com.maelespecieros.backend.entities.Caja;
import com.maelespecieros.backend.entities.EstadoCaja;
import com.maelespecieros.backend.entities.GastoOperativo;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import com.maelespecieros.backend.mappers.GastoOperativoMapper;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.GastoOperativoRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.GastoOperativoService;

@Service
@Transactional
public class GastoOperativoServiceImpl implements GastoOperativoService {

    private final GastoOperativoRepository gastoOperativoRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GastoOperativoMapper gastoOperativoMapper;
    private final BlockchainService blockchainService;

    public GastoOperativoServiceImpl(
            GastoOperativoRepository gastoOperativoRepository,
            CajaRepository cajaRepository,
            UsuarioRepository usuarioRepository,
            GastoOperativoMapper gastoOperativoMapper,
            BlockchainService blockchainService
    ) {
        this.gastoOperativoRepository = gastoOperativoRepository;
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.gastoOperativoMapper = gastoOperativoMapper;
        this.blockchainService = blockchainService;
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : null;

        if (username != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isPresent()) {
                return usuarioOpt.get();
            }
        }

        return usuarioRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("No existen usuarios registrados en el sistema para asociar al gasto."));
    }

    private String getUsernameAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
    }

    @Override
    public GastoOperativoResponse registrarGasto(GastoOperativoRequest request) {
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new BusinessException("No es posible registrar un gasto operativo: no existe ninguna caja abierta en el sistema."));

        Usuario usuario = getUsuarioActual();

        GastoOperativo gasto = new GastoOperativo();
        gasto.setMonto(request.monto());
        gasto.setConcepto(request.concepto().trim());
        gasto.setCategoriaGasto(request.categoriaGasto());
        gasto.setFormaPago(request.formaPago());
        gasto.setComprobanteNro(request.comprobanteNro() != null && !request.comprobanteNro().trim().isEmpty() ? request.comprobanteNro().trim() : null);
        gasto.setFecha(LocalDateTime.now());
        gasto.setAnulado(false);
        gasto.setCaja(caja);
        gasto.setUsuario(usuario);

        GastoOperativo guardado = gastoOperativoRepository.save(gasto);

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "REGISTRO_GASTO",
                "Registro de gasto operativo ID: " + guardado.getId() + " por $" + guardado.getMonto() + " (" + guardado.getConcepto() + ") en Turno de Caja ID: " + caja.getId(),
                "GASTO_OPERATIVO",
                guardado.getId().toString(),
                guardado
        );

        return gastoOperativoMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public GastoOperativoResponse obtenerPorId(Long id) {
        GastoOperativo gasto = gastoOperativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto operativo no encontrado con id: " + id));
        return gastoOperativoMapper.toDTO(gasto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoOperativoResponse> listarGastosCajaActual() {
        Optional<Caja> cajaOpt = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA);
        if (cajaOpt.isEmpty()) {
            return List.of();
        }
        return gastoOperativoRepository.findByCajaIdOrderByFechaDesc(cajaOpt.get().getId())
                .stream()
                .map(gastoOperativoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoOperativoResponse> listarGastosPorCaja(Long cajaId) {
        return gastoOperativoRepository.findByCajaIdOrderByFechaDesc(cajaId)
                .stream()
                .map(gastoOperativoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GastoOperativoResponse> listarHistorial(Pageable pageable) {
        return gastoOperativoRepository.findAllByOrderByFechaDesc(pageable)
                .map(gastoOperativoMapper::toDTO);
    }

    @Override
    public GastoOperativoResponse anularGasto(Long id) {
        GastoOperativo gasto = gastoOperativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto operativo no encontrado con id: " + id));

        if (Boolean.TRUE.equals(gasto.getAnulado())) {
            throw new BusinessException("El gasto operativo ya se encuentra anulado.");
        }

        if (gasto.getCaja().getEstado() == EstadoCaja.CERRADA) {
            throw new BusinessException("No es posible anular un gasto perteneciente a un turno de caja que ya ha sido cerrado.");
        }

        gasto.setAnulado(true);
        GastoOperativo guardado = gastoOperativoRepository.save(gasto);

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "ANULACION_GASTO",
                "Anulación de gasto operativo ID: " + guardado.getId() + " por $" + guardado.getMonto() + " en Caja ID: " + gasto.getCaja().getId(),
                "GASTO_OPERATIVO",
                guardado.getId().toString(),
                guardado
        );

        return gastoOperativoMapper.toDTO(guardado);
    }
}

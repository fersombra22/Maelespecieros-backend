package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.request.AperturaCajaDTO;
import com.maelespecieros.backend.dto.request.CierreCajaDTO;
import com.maelespecieros.backend.dto.response.CajaResponseDTO;
import com.maelespecieros.backend.dto.response.EstadoActualCajaDTO;
import com.maelespecieros.backend.entities.Caja;
import com.maelespecieros.backend.entities.EstadoCaja;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import com.maelespecieros.backend.mappers.CajaMapper;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.CajaService;

@Service
@Transactional
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaMapper cajaMapper;
    private final BlockchainService blockchainService;

    public CajaServiceImpl(
            CajaRepository cajaRepository,
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            CajaMapper cajaMapper,
            BlockchainService blockchainService
    ) {
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cajaMapper = cajaMapper;
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

        // Si no se encuentra por username o es ejecución interna, obtener el primer usuario del sistema
        return usuarioRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("No existen usuarios registrados en el sistema para asociar la caja."));
    }

    private String getUsernameAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
    }

    @Override
    public CajaResponseDTO abrirCaja(AperturaCajaDTO dto) {
        if (cajaRepository.existsByEstado(EstadoCaja.ABIERTA)) {
            throw new BusinessException("Ya existe una caja abierta en el sistema. Debe realizar el cierre antes de iniciar una nueva apertura.");
        }

        Usuario usuario = getUsuarioActual();

        Caja caja = new Caja();
        caja.setMontoInicial(dto.montoInicial());
        caja.setMontoFinal(null);
        caja.setMontoVentas(BigDecimal.ZERO);
        caja.setDiferencia(BigDecimal.ZERO);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setFechaCierre(null);
        caja.setEstado(EstadoCaja.ABIERTA);
        caja.setObservaciones(dto.observaciones() != null ? dto.observaciones().trim() : null);
        caja.setUsuario(usuario);

        Caja guardada = cajaRepository.save(caja);

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "APERTURA_CAJA",
                "Apertura de caja ID: " + guardada.getId() + " con monto inicial: $" + guardada.getMontoInicial(),
                "CAJA",
                guardada.getId().toString(),
                guardada
        );

        return cajaMapper.toDTO(guardada);
    }

    @Override
    public CajaResponseDTO cerrarCaja(CierreCajaDTO dto) {
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new BusinessException("No existe ninguna caja actualmente abierta para realizar el cierre."));

        LocalDateTime ahora = LocalDateTime.now();

        // Total facturado por ventas completadas desde la apertura hasta el momento de cierre
        BigDecimal montoVentas = ventaRepository.obtenerTotalFacturadoEntreFechas(caja.getFechaApertura(), ahora);
        if (montoVentas == null) {
            montoVentas = BigDecimal.ZERO;
        }

        BigDecimal montoEsperado = caja.getMontoInicial().add(montoVentas);
        BigDecimal diferencia = dto.montoFinal().subtract(montoEsperado);

        caja.setMontoVentas(montoVentas);
        caja.setMontoFinal(dto.montoFinal());
        caja.setDiferencia(diferencia);
        caja.setFechaCierre(ahora);
        caja.setEstado(EstadoCaja.CERRADA);

        if (dto.observaciones() != null && !dto.observaciones().trim().isEmpty()) {
            String obsNueva = dto.observaciones().trim();
            if (caja.getObservaciones() != null && !caja.getObservaciones().isEmpty()) {
                caja.setObservaciones(caja.getObservaciones() + " | Cierre: " + obsNueva);
            } else {
                caja.setObservaciones(obsNueva);
            }
        }

        Caja guardada = cajaRepository.save(caja);

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "CIERRE_CAJA",
                "Cierre de caja ID: " + guardada.getId() + ". Final: $" + guardada.getMontoFinal() + ", Ventas: $" + guardada.getMontoVentas() + ", Diferencia: $" + guardada.getDiferencia(),
                "CAJA",
                guardada.getId().toString(),
                guardada
        );

        return cajaMapper.toDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoActualCajaDTO obtenerEstadoActual() {
        Optional<Caja> cajaOpt = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA);

        if (cajaOpt.isEmpty()) {
            return cajaMapper.toEstadoActualDTO(false, null, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Caja caja = cajaOpt.get();
        BigDecimal montoVentasActual = ventaRepository.obtenerTotalFacturadoEntreFechas(caja.getFechaApertura(), LocalDateTime.now());
        if (montoVentasActual == null) {
            montoVentasActual = BigDecimal.ZERO;
        }

        BigDecimal montoEsperadoActual = caja.getMontoInicial().add(montoVentasActual);

        return cajaMapper.toEstadoActualDTO(true, caja, montoVentasActual, montoEsperadoActual);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponseDTO obtenerPorId(Long id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con id: " + id));
        return cajaMapper.toDTO(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponseDTO> listarHistorial(Pageable pageable) {
        return cajaRepository.findAllByOrderByFechaAperturaDesc(pageable)
                .map(cajaMapper::toDTO);
    }
}

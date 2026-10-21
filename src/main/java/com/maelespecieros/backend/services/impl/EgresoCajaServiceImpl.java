package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.request.EgresoCajaRequest;
import com.maelespecieros.backend.dto.response.EgresoCajaResponse;
import com.maelespecieros.backend.entities.Caja;
import com.maelespecieros.backend.entities.EgresoCaja;
import com.maelespecieros.backend.entities.EstadoCaja;
import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.TipoMedioEgreso;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.EgresoCajaRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.EgresoCajaService;

@Service
@Transactional
public class EgresoCajaServiceImpl implements EgresoCajaService {

    private static final Logger log = LoggerFactory.getLogger(EgresoCajaServiceImpl.class);

    private final EgresoCajaRepository egresoCajaRepository;
    private final CajaRepository cajaRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final BlockchainService blockchainService;

    public EgresoCajaServiceImpl(
            EgresoCajaRepository egresoCajaRepository,
            CajaRepository cajaRepository,
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            BlockchainService blockchainService
    ) {
        this.egresoCajaRepository = egresoCajaRepository;
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
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
                .orElseThrow(() -> new BusinessException("No existen usuarios registrados para asociar el egreso de caja."));
    }

    @Override
    public EgresoCajaResponse registrarEgreso(EgresoCajaRequest request) {
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new BusinessException("No es posible registrar egresos porque la caja se encuentra actualmente cerrada. Realice la apertura de caja antes de operar."));

        if (request.monto() == null || request.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto del egreso debe ser superior a cero.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        List<Object[]> ventasPorMetodo = ventaRepository.obtenerTotalesPorMetodoPagoEntreFechas(caja.getFechaApertura(), ahora);

        BigDecimal ventasEfectivo = BigDecimal.ZERO;
        BigDecimal ventasDigital = BigDecimal.ZERO;

        for (Object[] fila : ventasPorMetodo) {
            FormaPago forma = (FormaPago) fila[0];
            BigDecimal total = (BigDecimal) fila[1];
            if (forma != null && total != null) {
                if (forma == FormaPago.EFECTIVO) {
                    ventasEfectivo = ventasEfectivo.add(total);
                } else {
                    ventasDigital = ventasDigital.add(total);
                }
            }
        }

        BigDecimal egresosEfectivoPrevios = egresoCajaRepository.sumarPorCajaYTipoMedio(caja.getId(), TipoMedioEgreso.EFECTIVO);
        if (egresosEfectivoPrevios == null) egresosEfectivoPrevios = BigDecimal.ZERO;

        BigDecimal egresosDigitalPrevios = egresoCajaRepository.sumarPorCajaYTipoMedio(caja.getId(), TipoMedioEgreso.TRANSFERENCIA);
        if (egresosDigitalPrevios == null) egresosDigitalPrevios = BigDecimal.ZERO;

        BigDecimal fondoInicial = caja.getMontoInicial() != null ? caja.getMontoInicial() : BigDecimal.ZERO;

        if (request.tipoMedio() == TipoMedioEgreso.EFECTIVO) {
            BigDecimal efectivoDisponible = fondoInicial.add(ventasEfectivo).subtract(egresosEfectivoPrevios);
            if (request.monto().compareTo(efectivoDisponible) > 0) {
                throw new BusinessException("Fondos insuficientes en gaveta. El efectivo disponible actual es $" +
                        efectivoDisponible.setScale(2, java.math.RoundingMode.HALF_UP) +
                        " y se intentó retirar $" + request.monto().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        } else if (request.tipoMedio() == TipoMedioEgreso.TRANSFERENCIA) {
            BigDecimal digitalDisponible = ventasDigital.subtract(egresosDigitalPrevios);
            if (request.monto().compareTo(digitalDisponible) > 0) {
                throw new BusinessException("Fondos digitales insuficientes en el turno. El saldo disponible de cobranzas virtuales es $" +
                        digitalDisponible.setScale(2, java.math.RoundingMode.HALF_UP) +
                        " y se intentó transferir $" + request.monto().setScale(2, java.math.RoundingMode.HALF_UP));
            }
        }

        Usuario usuario = getUsuarioActual();

        EgresoCaja egreso = new EgresoCaja();
        egreso.setCaja(caja);
        egreso.setUsuario(usuario);
        egreso.setMonto(request.monto());
        egreso.setTipoMedio(request.tipoMedio());
        egreso.setMotivo(request.motivo().trim());
        egreso.setComprobante(request.comprobante() != null ? request.comprobante().trim() : null);
        egreso.setFecha(ahora);
        egreso.firmarIntegridad();

        EgresoCaja guardado = egresoCajaRepository.save(egreso);

        // Registro de Auditoría Inmutable en Blockchain
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("egresoId", guardado.getId());
        payload.put("cajaId", caja.getId());
        payload.put("monto", guardado.getMonto());
        payload.put("tipoMedio", guardado.getTipoMedio().name());
        payload.put("motivo", guardado.getMotivo());
        payload.put("comprobante", guardado.getComprobante());
        payload.put("fecha", guardado.getFecha().toString());
        payload.put("cajero", usuario.getUsername());

        blockchainService.registrarBloque(
                usuario.getUsername(),
                "EGRESO_CAJA",
                "Egreso de caja #" + caja.getId() + " por $" + guardado.getMonto() + " (" + guardado.getTipoMedio() + ") - Motivo: " + guardado.getMotivo(),
                "CAJA",
                caja.getId().toString(),
                payload
        );

        log.info("Egreso de caja registrado exitosamente: ID {}, Monto ${}, Medio {}, Caja ID {}",
                guardado.getId(), guardado.getMonto(), guardado.getTipoMedio(), caja.getId());

        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EgresoCajaResponse> listarEgresosCajaActual() {
        Optional<Caja> cajaOpt = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA);
        if (cajaOpt.isEmpty()) {
            return List.of();
        }
        return egresoCajaRepository.findByCajaIdOrderByFechaDesc(cajaOpt.get().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EgresoCajaResponse> listarEgresosPorCaja(Long cajaId) {
        return egresoCajaRepository.findByCajaIdOrderByFechaDesc(cajaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EgresoCajaResponse toResponse(EgresoCaja egreso) {
        return new EgresoCajaResponse(
                egreso.getId(),
                egreso.getCaja().getId(),
                egreso.getMonto(),
                egreso.getTipoMedio(),
                egreso.getMotivo(),
                egreso.getComprobante(),
                egreso.getFecha(),
                egreso.getUsuario().getId(),
                egreso.getUsuario().getNombre() != null ? egreso.getUsuario().getNombre() : egreso.getUsuario().getUsername()
        );
    }
}

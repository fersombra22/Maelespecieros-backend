package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.TipoMedioEgreso;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import com.maelespecieros.backend.mappers.CajaMapper;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.EgresoCajaRepository;
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
    private final EgresoCajaRepository egresoCajaRepository;
    private final CajaMapper cajaMapper;
    private final BlockchainService blockchainService;

    public CajaServiceImpl(
            CajaRepository cajaRepository,
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            EgresoCajaRepository egresoCajaRepository,
            CajaMapper cajaMapper,
            BlockchainService blockchainService
    ) {
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.egresoCajaRepository = egresoCajaRepository;
        this.cajaMapper = cajaMapper;
        this.blockchainService = blockchainService;
    }

    private static record DesgloseTurno(
            BigDecimal totalEfectivo,
            BigDecimal totalDebito,
            BigDecimal totalCredito,
            BigDecimal totalTransferencia,
            BigDecimal totalDigital,
            BigDecimal totalVentas,
            Long cantidadVentas
    ) {}

    private DesgloseTurno calcularDesglose(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null) {
            return new DesgloseTurno(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        }
        LocalDateTime fechaFin = fin != null ? fin : LocalDateTime.now();

        List<Object[]> resultados = ventaRepository.obtenerTotalesPorMetodoPagoEntreFechas(inicio, fechaFin);
        Long cantidadVentas = ventaRepository.contarVentasEntreFechas(inicio, fechaFin);
        if (cantidadVentas == null) {
            cantidadVentas = 0L;
        }

        BigDecimal efectivo = BigDecimal.ZERO;
        BigDecimal debito = BigDecimal.ZERO;
        BigDecimal credito = BigDecimal.ZERO;
        BigDecimal transferencia = BigDecimal.ZERO;

        for (Object[] fila : resultados) {
            FormaPago forma = (FormaPago) fila[0];
            BigDecimal total = (BigDecimal) fila[1];
            if (forma != null && total != null) {
                switch (forma) {
                    case EFECTIVO -> efectivo = efectivo.add(total);
                    case DEBITO -> debito = debito.add(total);
                    case CREDITO -> credito = credito.add(total);
                    case TRANSFERENCIA -> transferencia = transferencia.add(total);
                }
            }
        }

        BigDecimal digital = debito.add(credito).add(transferencia);
        BigDecimal totalVentas = efectivo.add(digital);

        return new DesgloseTurno(efectivo, debito, credito, transferencia, digital, totalVentas, cantidadVentas);
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

        Map<String, Object> payloadApertura = new LinkedHashMap<>();
        payloadApertura.put("cajaId", guardada.getId());
        payloadApertura.put("montoInicial", guardada.getMontoInicial());
        payloadApertura.put("fechaApertura", guardada.getFechaApertura() != null ? guardada.getFechaApertura().toString() : null);
        payloadApertura.put("cajero", guardada.getUsuario() != null ? guardada.getUsuario().getUsername() : "SISTEMA");
        payloadApertura.put("observaciones", guardada.getObservaciones());

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "APERTURA_CAJA",
                "Apertura de caja ID: " + guardada.getId() + " con monto inicial: $" + guardada.getMontoInicial(),
                "CAJA",
                guardada.getId().toString(),
                payloadApertura
        );

        return cajaMapper.toDTO(guardada, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    public CajaResponseDTO cerrarCaja(CierreCajaDTO dto) {
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new BusinessException("No existe ninguna caja actualmente abierta para realizar el cierre."));

        LocalDateTime ahora = LocalDateTime.now();
        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), ahora);

        BigDecimal egresosEfectivo = egresoCajaRepository.sumarPorCajaYTipoMedio(caja.getId(), TipoMedioEgreso.EFECTIVO);
        if (egresosEfectivo == null) egresosEfectivo = BigDecimal.ZERO;

        BigDecimal egresosDigital = egresoCajaRepository.sumarPorCajaYTipoMedio(caja.getId(), TipoMedioEgreso.TRANSFERENCIA);
        if (egresosDigital == null) egresosDigital = BigDecimal.ZERO;

        BigDecimal totalEgresos = egresosEfectivo.add(egresosDigital);

        BigDecimal montoInicial = caja.getMontoInicial() != null ? caja.getMontoInicial() : BigDecimal.ZERO;
        BigDecimal montoVentas = desglose.totalVentas();
        // Efectivo esperado en cajón físico restando los egresos en efectivo
        BigDecimal efectivoEsperado = montoInicial.add(desglose.totalEfectivo()).subtract(egresosEfectivo);
        // Monto total esperado del turno restando todos los egresos
        BigDecimal montoEsperadoTotal = montoInicial.add(montoVentas).subtract(totalEgresos);

        BigDecimal montoFinal;
        BigDecimal diferencia;

        if (dto.montoEfectivo() != null) {
            // El cajero ingresó el efectivo físico contado en el cajón
            BigDecimal efectivoContado = dto.montoEfectivo();
            // Total rendido = Efectivo físico contado + Ventas digitales netas de transferencias
            montoFinal = efectivoContado.add(desglose.totalDigital().subtract(egresosDigital));
            // La diferencia es el arqueo sobre el efectivo: efectivoContado - efectivoEsperado (fondo inicial + ventas efectivo - egresos efectivo)
            diferencia = efectivoContado.subtract(efectivoEsperado);
        } else if (dto.montoFinal() != null) {
            montoFinal = dto.montoFinal();
            diferencia = montoFinal.subtract(montoEsperadoTotal);
        } else {
            throw new BusinessException("Debe ingresar el efectivo contado o el monto final de cierre.");
        }

        caja.setMontoVentas(montoVentas);
        caja.setMontoFinal(montoFinal);
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

        Map<String, Object> payloadCierre = new LinkedHashMap<>();
        payloadCierre.put("cajaId", guardada.getId());
        payloadCierre.put("montoInicial", guardada.getMontoInicial());
        payloadCierre.put("montoFinal", guardada.getMontoFinal());
        payloadCierre.put("montoVentas", guardada.getMontoVentas());
        payloadCierre.put("diferencia", guardada.getDiferencia());
        payloadCierre.put("fechaApertura", guardada.getFechaApertura() != null ? guardada.getFechaApertura().toString() : null);
        payloadCierre.put("fechaCierre", guardada.getFechaCierre() != null ? guardada.getFechaCierre().toString() : null);
        payloadCierre.put("cajero", guardada.getUsuario() != null ? guardada.getUsuario().getUsername() : "SISTEMA");
        payloadCierre.put("totalEfectivo", desglose.totalEfectivo());
        payloadCierre.put("totalDigital", desglose.totalDigital());
        payloadCierre.put("totalEgresosEfectivo", egresosEfectivo);
        payloadCierre.put("totalEgresosDigital", egresosDigital);
        payloadCierre.put("totalEgresos", totalEgresos);
        payloadCierre.put("cantidadVentas", desglose.cantidadVentas());
        payloadCierre.put("observaciones", guardada.getObservaciones());

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "CIERRE_CAJA",
                "Cierre de caja ID: " + guardada.getId() + ". Final: $" + guardada.getMontoFinal() + ", Ventas: $" + guardada.getMontoVentas() + ", Egresos: $" + totalEgresos + ", Diferencia: $" + guardada.getDiferencia(),
                "CAJA",
                guardada.getId().toString(),
                payloadCierre
        );

        return cajaMapper.toDTO(
                guardada,
                desglose.totalEfectivo(),
                desglose.totalDebito(),
                desglose.totalCredito(),
                desglose.totalTransferencia(),
                desglose.totalDigital(),
                desglose.cantidadVentas(),
                egresosEfectivo,
                egresosDigital,
                totalEgresos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoActualCajaDTO obtenerEstadoActual() {
        Optional<Caja> cajaOpt = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA);

        if (cajaOpt.isEmpty()) {
            return cajaMapper.toEstadoActualDTO(
                    false,
                    null,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        Caja caja = cajaOpt.get();
        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), LocalDateTime.now());

        BigDecimal egresosEfectivo = egresoCajaRepository.sumarPorCajaYTipoMedio(caja.getId(), TipoMedioEgreso.EFECTIVO);
        if (egresosEfectivo == null) egresosEfectivo = BigDecimal.ZERO;

        BigDecimal egresosDigital = egresoCajaRepository.sumarPorCajaYTipoMedio(caja.getId(), TipoMedioEgreso.TRANSFERENCIA);
        if (egresosDigital == null) egresosDigital = BigDecimal.ZERO;

        BigDecimal totalEgresos = egresosEfectivo.add(egresosDigital);

        BigDecimal montoInicial = caja.getMontoInicial() != null ? caja.getMontoInicial() : BigDecimal.ZERO;
        BigDecimal montoEsperadoTotal = montoInicial.add(desglose.totalVentas()).subtract(totalEgresos);

        return cajaMapper.toEstadoActualDTO(
                true,
                caja,
                desglose.totalVentas(),
                montoEsperadoTotal,
                desglose.totalEfectivo(),
                desglose.totalDebito(),
                desglose.totalCredito(),
                desglose.totalTransferencia(),
                desglose.totalDigital(),
                desglose.cantidadVentas(),
                egresosEfectivo,
                egresosDigital,
                totalEgresos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponseDTO obtenerPorId(Long id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con id: " + id));
        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), caja.getFechaCierre());
        return cajaMapper.toDTO(
                caja,
                desglose.totalEfectivo(),
                desglose.totalDebito(),
                desglose.totalCredito(),
                desglose.totalTransferencia(),
                desglose.totalDigital(),
                desglose.cantidadVentas()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponseDTO> listarHistorial(Pageable pageable) {
        return cajaRepository.findAllByOrderByFechaAperturaDesc(pageable)
                .map(caja -> {
                    // Si el turno está en curso (abierto), calculamos las métricas dinámicas
                    if (caja.getEstado() == EstadoCaja.ABIERTA) {
                        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), LocalDateTime.now());
                        return cajaMapper.toDTO(
                                caja,
                                desglose.totalEfectivo(),
                                desglose.totalDebito(),
                                desglose.totalCredito(),
                                desglose.totalTransferencia(),
                                desglose.totalDigital(),
                                desglose.cantidadVentas()
                        );
                    }
                    // Para turnos cerrados, sus totales ya están consolidados en la entidad;
                    // se mapean directamente sin ejecutar consultas agregadas redundantes sobre ventas
                    return cajaMapper.toDTO(caja);
                });
    }
}

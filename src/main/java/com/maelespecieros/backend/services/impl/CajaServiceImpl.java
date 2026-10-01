package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import com.maelespecieros.backend.mappers.CajaMapper;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.GastoOperativoRepository;
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
    private final GastoOperativoRepository gastoOperativoRepository;
    private final CajaMapper cajaMapper;
    private final BlockchainService blockchainService;

    public CajaServiceImpl(
            CajaRepository cajaRepository,
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            GastoOperativoRepository gastoOperativoRepository,
            CajaMapper cajaMapper,
            BlockchainService blockchainService
    ) {
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.gastoOperativoRepository = gastoOperativoRepository;
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

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "APERTURA_CAJA",
                "Apertura de caja ID: " + guardada.getId() + " con monto inicial: $" + guardada.getMontoInicial(),
                "CAJA",
                guardada.getId().toString(),
                guardada
        );

        return cajaMapper.toDTO(guardada, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }

    @Override
    public CajaResponseDTO cerrarCaja(CierreCajaDTO dto) {
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElseThrow(() -> new BusinessException("No existe ninguna caja actualmente abierta para realizar el cierre."));

        LocalDateTime ahora = LocalDateTime.now();
        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), ahora);

        BigDecimal totalEgresos = gastoOperativoRepository.obtenerTotalEgresosPorCaja(caja.getId());
        BigDecimal totalEgresosEfectivo = gastoOperativoRepository.obtenerTotalEgresosPorCajaYFormaPago(caja.getId(), FormaPago.EFECTIVO);

        BigDecimal montoVentas = desglose.totalVentas();

        // Fórmula del efectivo esperado: Monto inicial + Ventas en efectivo - Egresos en efectivo
        BigDecimal efectivoEsperado = caja.getMontoInicial().add(desglose.totalEfectivo()).subtract(totalEgresosEfectivo);

        BigDecimal montoFinal;
        BigDecimal diferencia;

        if (dto.montoEfectivo() != null) {
            // El cajero ingresó el efectivo físico contado en el cajón
            BigDecimal efectivoContado = dto.montoEfectivo();
            // Total rendido = Efectivo físico contado + Ventas digitales registradas automáticamente
            montoFinal = efectivoContado.add(desglose.totalDigital());
            // La diferencia es el arqueo sobre el efectivo: efectivoContado - efectivoEsperado
            diferencia = efectivoContado.subtract(efectivoEsperado);
        } else if (dto.montoFinal() != null) {
            montoFinal = dto.montoFinal();
            diferencia = montoFinal.subtract(montoVentas);
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

        blockchainService.registrarBloque(
                getUsernameAutenticado(),
                "CIERRE_CAJA",
                "Cierre de caja ID: " + guardada.getId() + ". Final: $" + guardada.getMontoFinal() + ", Ventas: $" + guardada.getMontoVentas() + ", Diferencia: $" + guardada.getDiferencia(),
                "CAJA",
                guardada.getId().toString(),
                guardada
        );

        return cajaMapper.toDTO(
                guardada,
                desglose.totalEfectivo(),
                desglose.totalDebito(),
                desglose.totalCredito(),
                desglose.totalTransferencia(),
                desglose.totalDigital(),
                desglose.cantidadVentas(),
                totalEgresos,
                totalEgresosEfectivo
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
                    BigDecimal.ZERO
            );
        }

        Caja caja = cajaOpt.get();
        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), LocalDateTime.now());

        BigDecimal totalEgresos = gastoOperativoRepository.obtenerTotalEgresosPorCaja(caja.getId());
        BigDecimal totalEgresosEfectivo = gastoOperativoRepository.obtenerTotalEgresosPorCajaYFormaPago(caja.getId(), FormaPago.EFECTIVO);

        // Fórmula: Monto inicial + Ventas en efectivo - Egresos en efectivo
        BigDecimal efectivoEsperado = caja.getMontoInicial().add(desglose.totalEfectivo()).subtract(totalEgresosEfectivo);

        return cajaMapper.toEstadoActualDTO(
                true,
                caja,
                desglose.totalVentas(),
                efectivoEsperado,
                desglose.totalEfectivo(),
                desglose.totalDebito(),
                desglose.totalCredito(),
                desglose.totalTransferencia(),
                desglose.totalDigital(),
                desglose.cantidadVentas(),
                totalEgresos,
                totalEgresosEfectivo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponseDTO obtenerPorId(Long id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con id: " + id));
        DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), caja.getFechaCierre());
        BigDecimal totalEgresos = gastoOperativoRepository.obtenerTotalEgresosPorCaja(caja.getId());
        BigDecimal totalEgresosEfectivo = gastoOperativoRepository.obtenerTotalEgresosPorCajaYFormaPago(caja.getId(), FormaPago.EFECTIVO);

        return cajaMapper.toDTO(
                caja,
                desglose.totalEfectivo(),
                desglose.totalDebito(),
                desglose.totalCredito(),
                desglose.totalTransferencia(),
                desglose.totalDigital(),
                desglose.cantidadVentas(),
                totalEgresos,
                totalEgresosEfectivo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponseDTO> listarHistorial(Pageable pageable) {
        return cajaRepository.findAllByOrderByFechaAperturaDesc(pageable)
                .map(caja -> {
                    DesgloseTurno desglose = calcularDesglose(caja.getFechaApertura(), caja.getFechaCierre());
                    BigDecimal totalEgresos = gastoOperativoRepository.obtenerTotalEgresosPorCaja(caja.getId());
                    BigDecimal totalEgresosEfectivo = gastoOperativoRepository.obtenerTotalEgresosPorCajaYFormaPago(caja.getId(), FormaPago.EFECTIVO);
                    return cajaMapper.toDTO(
                            caja,
                            desglose.totalEfectivo(),
                            desglose.totalDebito(),
                            desglose.totalCredito(),
                            desglose.totalTransferencia(),
                            desglose.totalDigital(),
                            desglose.cantidadVentas(),
                            totalEgresos,
                            totalEgresosEfectivo
                    );
                });
    }
}

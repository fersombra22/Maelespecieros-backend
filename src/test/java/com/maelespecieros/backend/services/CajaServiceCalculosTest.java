package com.maelespecieros.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.maelespecieros.backend.dto.request.CierreCajaDTO;
import com.maelespecieros.backend.dto.response.CajaResponseDTO;
import com.maelespecieros.backend.dto.response.EstadoActualCajaDTO;
import com.maelespecieros.backend.entities.Caja;
import com.maelespecieros.backend.entities.EstadoCaja;
import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.mappers.CajaMapper;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.GastoOperativoRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.impl.CajaServiceImpl;

@ExtendWith(MockitoExtension.class)
class CajaServiceCalculosTest {

    @Mock
    private CajaRepository cajaRepository;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GastoOperativoRepository gastoOperativoRepository;

    @Mock
    private CajaMapper cajaMapper;

    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private CajaServiceImpl cajaService;

    private Caja caja;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setNombre("Administrador");

        caja = new Caja();
        caja.setId(10L);
        caja.setMontoInicial(new BigDecimal("5000.00"));
        caja.setFechaApertura(LocalDateTime.now().minusHours(4));
        caja.setEstado(EstadoCaja.ABIERTA);
        caja.setUsuario(usuario);
    }

    private void configurarSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Cálculo de efectivo esperado descuenta solo egresos en efectivo y no digitales")
    void calculoEfectivoEsperado_descuentaEgresosEfectivoYNoDigitales() {
        when(cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA))
                .thenReturn(Optional.of(caja));

        // Ventas: Efectivo 10000, Transferencia 3000
        List<Object[]> ventasDesglose = List.of(
                new Object[]{FormaPago.EFECTIVO, new BigDecimal("10000.00")},
                new Object[]{FormaPago.TRANSFERENCIA, new BigDecimal("3000.00")}
        );
        when(ventaRepository.obtenerTotalesPorMetodoPagoEntreFechas(any(), any()))
                .thenReturn(ventasDesglose);
        when(ventaRepository.contarVentasEntreFechas(any(), any())).thenReturn(5L);

        // Egresos: Total 2000 (1500 en efectivo, 500 en transferencia)
        when(gastoOperativoRepository.obtenerTotalEgresosPorCaja(10L))
                .thenReturn(new BigDecimal("2000.00"));
        when(gastoOperativoRepository.obtenerTotalEgresosPorCajaYFormaPago(10L, FormaPago.EFECTIVO))
                .thenReturn(new BigDecimal("1500.00"));

        when(cajaMapper.toEstadoActualDTO(anyBoolean(), any(Caja.class), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    BigDecimal efectivoEsperadoArg = inv.getArgument(3);
                    return new EstadoActualCajaDTO(
                            true,
                            null,
                            new BigDecimal("13000.00"),
                            efectivoEsperadoArg,
                            new BigDecimal("10000.00"),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            new BigDecimal("3000.00"),
                            new BigDecimal("3000.00"),
                            5L,
                            new BigDecimal("2000.00"),
                            new BigDecimal("1500.00")
                    );
                });

        EstadoActualCajaDTO estado = cajaService.obtenerEstadoActual();

        assertNotNull(estado);
        // Efectivo esperado = 5000 (inicial) + 10000 (ventas efectivo) - 1500 (egresos efectivo) = 13500.00
        // (Los 500 de egreso en transferencia NO deben descontar del efectivo físico de gaveta)
        assertEquals(new BigDecimal("13500.00"), estado.montoEsperadoActual());
        assertEquals(new BigDecimal("1500.00"), estado.totalEgresosEfectivoActual());
        assertEquals(new BigDecimal("2000.00"), estado.totalEgresosActual());
    }

    @Test
    @DisplayName("Cierre de caja con arqueo de efectivo calcula correctamente monto final y diferencia")
    void cerrarCaja_conMontoEfectivo_calculaArqueoYDiferencia() {
        configurarSecurityContext();

        when(cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA))
                .thenReturn(Optional.of(caja));

        List<Object[]> ventasDesglose = List.of(
                new Object[]{FormaPago.EFECTIVO, new BigDecimal("10000.00")},
                new Object[]{FormaPago.TRANSFERENCIA, new BigDecimal("2000.00")}
        );
        when(ventaRepository.obtenerTotalesPorMetodoPagoEntreFechas(any(), any()))
                .thenReturn(ventasDesglose);
        when(ventaRepository.contarVentasEntreFechas(any(), any())).thenReturn(3L);

        // Egresos en efectivo = 1000.00
        when(gastoOperativoRepository.obtenerTotalEgresosPorCaja(10L))
                .thenReturn(new BigDecimal("1000.00"));
        when(gastoOperativoRepository.obtenerTotalEgresosPorCajaYFormaPago(10L, FormaPago.EFECTIVO))
                .thenReturn(new BigDecimal("1000.00"));

        when(cajaRepository.save(any(Caja.class))).thenAnswer(i -> i.getArgument(0));

        // Efectivo contado en gaveta = 14000.00
        // Efectivo esperado = 5000 + 10000 - 1000 = 14000.00
        // Diferencia = 14000 - 14000 = 0.00
        CierreCajaDTO dto = new CierreCajaDTO(null, new BigDecimal("14000.00"), "Cierre exacto");

        when(cajaMapper.toDTO(any(Caja.class), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    Caja c = inv.getArgument(0);
                    return new CajaResponseDTO(
                            c.getId(),
                            c.getMontoInicial(),
                            c.getMontoFinal(),
                            c.getMontoVentas(),
                            c.getDiferencia(),
                            c.getFechaApertura(),
                            c.getFechaCierre(),
                            c.getEstado(),
                            c.getObservaciones(),
                            Long.valueOf(1L),
                            "admin",
                            "Admin",
                            new BigDecimal("10000.00"),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            new BigDecimal("2000.00"),
                            new BigDecimal("2000.00"),
                            Long.valueOf(3L),
                            new BigDecimal("1000.00"),
                            new BigDecimal("1000.00")
                    );
                });

        CajaResponseDTO response = cajaService.cerrarCaja(dto);

        assertNotNull(response);
        assertEquals(EstadoCaja.CERRADA, caja.getEstado());
        assertEquals(new BigDecimal("0.00"), caja.getDiferencia());
        // Monto final rendido = 14000 (efectivo contado) + 2000 (ventas digitales) = 16000.00
        assertEquals(new BigDecimal("16000.00"), caja.getMontoFinal());
    }
}

package com.maelespecieros.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.maelespecieros.backend.dto.audit.GastoOperativoAuditDTO;
import com.maelespecieros.backend.dto.request.GastoOperativoRequest;
import com.maelespecieros.backend.dto.response.GastoOperativoResponse;
import com.maelespecieros.backend.entities.Caja;
import com.maelespecieros.backend.entities.CategoriaGasto;
import com.maelespecieros.backend.entities.EstadoCaja;
import com.maelespecieros.backend.entities.FormaPago;
import com.maelespecieros.backend.entities.GastoOperativo;
import com.maelespecieros.backend.entities.Usuario;
import com.maelespecieros.backend.exceptions.BusinessException;
import com.maelespecieros.backend.mappers.GastoOperativoMapper;
import com.maelespecieros.backend.repositories.CajaRepository;
import com.maelespecieros.backend.repositories.GastoOperativoRepository;
import com.maelespecieros.backend.repositories.UsuarioRepository;
import com.maelespecieros.backend.services.impl.GastoOperativoServiceImpl;

@ExtendWith(MockitoExtension.class)
class GastoOperativoServiceTest {

    @Mock
    private GastoOperativoRepository gastoOperativoRepository;

    @Mock
    private CajaRepository cajaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GastoOperativoMapper gastoOperativoMapper;

    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private GastoOperativoServiceImpl gastoOperativoService;

    private Caja cajaAbierta;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        cajaAbierta = new Caja();
        cajaAbierta.setId(10L);
        cajaAbierta.setMontoInicial(new BigDecimal("1000.00"));
        cajaAbierta.setEstado(EstadoCaja.ABIERTA);

        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("cajero_test");
        usuarioMock.setNombre("Juan Cajero");
    }

    private void configurarSecurityContext(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("1. Registrar gasto con caja abierta registra exitosamente y genera payload de auditoría seguro")
    void registrarGasto_conCajaAbierta_registraYGeneraPayloadSeguro() {
        configurarSecurityContext("cajero_test");
        when(cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA))
                .thenReturn(Optional.of(cajaAbierta));
        when(usuarioRepository.findByUsername("cajero_test"))
                .thenReturn(Optional.of(usuarioMock));

        GastoOperativoRequest request = new GastoOperativoRequest(
                new BigDecimal("250.00"),
                "Compra de cinta adhesiva y etiquetas",
                CategoriaGasto.INSUMOS,
                FormaPago.EFECTIVO,
                "FAC-001"
        );

        when(gastoOperativoRepository.save(any(GastoOperativo.class))).thenAnswer(invocation -> {
            GastoOperativo g = invocation.getArgument(0);
            g.setId(100L);
            g.firmarIntegridad();
            return g;
        });

        GastoOperativoResponse mockResponse = new GastoOperativoResponse(
                100L,
                new BigDecimal("250.00"),
                "Compra de cinta adhesiva y etiquetas",
                CategoriaGasto.INSUMOS,
                FormaPago.EFECTIVO,
                "FAC-001",
                LocalDateTime.now(),
                false,
                10L,
                1L,
                "cajero_test",
                "Juan Cajero"
        );
        when(gastoOperativoMapper.toDTO(any(GastoOperativo.class))).thenReturn(mockResponse);

        GastoOperativoResponse resultado = gastoOperativoService.registrarGasto(request);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("250.00"), resultado.monto());
        assertEquals("Compra de cinta adhesiva y etiquetas", resultado.concepto());

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(blockchainService).registrarBloque(
                eq("cajero_test"),
                eq("REGISTRO_GASTO"),
                any(),
                eq("GASTO_OPERATIVO"),
                eq("100"),
                payloadCaptor.capture()
        );

        assertTrue(payloadCaptor.getValue() instanceof GastoOperativoAuditDTO);
        GastoOperativoAuditDTO auditDto = (GastoOperativoAuditDTO) payloadCaptor.getValue();
        assertEquals(100L, auditDto.id());
        assertEquals(new BigDecimal("250.00"), auditDto.monto());
        assertEquals("cajero_test", auditDto.usuarioUsername());
        assertNotNull(auditDto.hashIntegridad());
    }

    @Test
    @DisplayName("2. Registrar gasto sin caja abierta lanza BusinessException")
    void registrarGasto_sinCajaAbierta_lanzaBusinessException() {
        when(cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA))
                .thenReturn(Optional.empty());

        GastoOperativoRequest request = new GastoOperativoRequest(
                new BigDecimal("150.00"),
                "Artículos de limpieza",
                CategoriaGasto.INSUMOS,
                FormaPago.EFECTIVO,
                null
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                gastoOperativoService.registrarGasto(request)
        );

        assertTrue(ex.getMessage().contains("no existe ninguna caja abierta"));
        verify(gastoOperativoRepository, never()).save(any());
        verify(blockchainService, never()).registrarBloque(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("3. Registrar gasto con monto negativo o cero lanza BusinessException")
    void registrarGasto_montoInvalido_lanzaBusinessException() {
        GastoOperativoRequest requestCero = new GastoOperativoRequest(
                BigDecimal.ZERO,
                "Insumos",
                CategoriaGasto.INSUMOS,
                FormaPago.EFECTIVO,
                null
        );

        assertThrows(BusinessException.class, () ->
                gastoOperativoService.registrarGasto(requestCero)
        );
    }

    @Test
    @DisplayName("4. Anular gasto con caja abierta actualiza estado a anulado y registra en blockchain")
    void anularGasto_conCajaAbierta_actualizaEstadoYRegistraBlockchain() {
        configurarSecurityContext("cajero_test");

        GastoOperativo gastoExistente = new GastoOperativo();
        gastoExistente.setId(50L);
        gastoExistente.setMonto(new BigDecimal("300.00"));
        gastoExistente.setConcepto("Taxi reparto");
        gastoExistente.setCategoriaGasto(CategoriaGasto.VARIOS);
        gastoExistente.setFormaPago(FormaPago.EFECTIVO);
        gastoExistente.setAnulado(false);
        gastoExistente.setCaja(cajaAbierta);
        gastoExistente.setUsuario(usuarioMock);
        gastoExistente.firmarIntegridad();

        when(gastoOperativoRepository.findById(50L)).thenReturn(Optional.of(gastoExistente));
        when(gastoOperativoRepository.save(any(GastoOperativo.class))).thenAnswer(i -> i.getArgument(0));

        GastoOperativoResponse mockResponse = new GastoOperativoResponse(
                50L,
                new BigDecimal("300.00"),
                "Taxi reparto",
                CategoriaGasto.VARIOS,
                FormaPago.EFECTIVO,
                null,
                LocalDateTime.now(),
                true,
                10L,
                1L,
                "cajero_test",
                "Juan Cajero"
        );
        when(gastoOperativoMapper.toDTO(any())).thenReturn(mockResponse);

        GastoOperativoResponse resultado = gastoOperativoService.anularGasto(50L);

        assertTrue(resultado.anulado());
        assertTrue(gastoExistente.getAnulado());
        verify(blockchainService).registrarBloque(
                eq("cajero_test"),
                eq("ANULACION_GASTO"),
                any(),
                eq("GASTO_OPERATIVO"),
                eq("50"),
                any(GastoOperativoAuditDTO.class)
        );
    }

    @Test
    @DisplayName("5. Anular gasto de una caja ya cerrada lanza BusinessException")
    void anularGasto_cajaCerrada_lanzaBusinessException() {
        Caja cajaCerrada = new Caja();
        cajaCerrada.setId(9L);
        cajaCerrada.setEstado(EstadoCaja.CERRADA);

        GastoOperativo gastoTurnoCerrado = new GastoOperativo();
        gastoTurnoCerrado.setId(20L);
        gastoTurnoCerrado.setCaja(cajaCerrada);
        gastoTurnoCerrado.setAnulado(false);

        when(gastoOperativoRepository.findById(20L)).thenReturn(Optional.of(gastoTurnoCerrado));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                gastoOperativoService.anularGasto(20L)
        );

        assertTrue(ex.getMessage().contains("ya ha sido cerrado"));
        verify(gastoOperativoRepository, never()).save(any());
    }

    @Test
    @DisplayName("6. Hash de integridad de GastoOperativo es determinístico antes y después de persistir y anular")
    void hashIntegridad_esDeterministicoYValidoEnTodoElCiclo() {
        GastoOperativo gasto = new GastoOperativo();
        gasto.setMonto(new BigDecimal("500.00"));
        gasto.setConcepto("Pago de internet");
        gasto.setCategoriaGasto(CategoriaGasto.SERVICIOS);
        gasto.setFormaPago(FormaPago.TRANSFERENCIA);
        gasto.setCaja(cajaAbierta);
        gasto.setUsuario(usuarioMock);

        // 1. Antes de persistir: id es null
        assertNull(gasto.getId());
        gasto.prePersist();
        assertNotNull(gasto.getHashIntegridad());
        assertTrue(gasto.esIntegro(), "Debe ser íntegro antes de persistir");

        // 2. Simulación de JPA tras INSERT: SQLite asigna id = 42L
        gasto.setId(42L);
        assertTrue(gasto.esIntegro(), "Debe seguir siendo íntegro con el ID asignado por la base de datos");

        // 3. Modificación ilícita: alteración de monto
        gasto.setMonto(new BigDecimal("9999.00"));
        assertFalse(gasto.esIntegro(), "La alteración de monto debe invalidar la firma");

        // Restaurar monto
        gasto.setMonto(new BigDecimal("500.00"));
        assertTrue(gasto.esIntegro(), "Al restaurar el monto original vuelve a ser íntegro");

        // 4. Anulación autorizada: preUpdate recalcula hash con anulado = true
        gasto.setAnulado(true);
        gasto.preUpdate();
        assertTrue(gasto.esIntegro(), "Debe ser íntegro después de anular con el nuevo hash actualizado");
    }
}

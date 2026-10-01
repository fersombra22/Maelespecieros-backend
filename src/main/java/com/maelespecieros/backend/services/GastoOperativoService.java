package com.maelespecieros.backend.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maelespecieros.backend.dto.request.GastoOperativoRequest;
import com.maelespecieros.backend.dto.response.GastoOperativoResponse;

public interface GastoOperativoService {

    GastoOperativoResponse registrarGasto(GastoOperativoRequest request);

    GastoOperativoResponse obtenerPorId(Long id);

    List<GastoOperativoResponse> listarGastosCajaActual();

    List<GastoOperativoResponse> listarGastosPorCaja(Long cajaId);

    Page<GastoOperativoResponse> listarHistorial(Pageable pageable);

    GastoOperativoResponse anularGasto(Long id);
}

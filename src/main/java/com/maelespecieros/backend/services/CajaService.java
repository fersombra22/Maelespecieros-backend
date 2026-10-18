package com.maelespecieros.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maelespecieros.backend.dto.request.AperturaCajaDTO;
import com.maelespecieros.backend.dto.request.CierreCajaDTO;
import com.maelespecieros.backend.dto.response.CajaResponseDTO;
import com.maelespecieros.backend.dto.response.EstadoActualCajaDTO;

public interface CajaService {

    CajaResponseDTO abrirCaja(AperturaCajaDTO dto);

    CajaResponseDTO cerrarCaja(CierreCajaDTO dto);

    EstadoActualCajaDTO obtenerEstadoActual();

    CajaResponseDTO obtenerPorId(Long id);

    Page<CajaResponseDTO> listarHistorial(Pageable pageable);
}

package com.maelespecieros.backend.services;

import java.util.List;

import com.maelespecieros.backend.dto.request.EgresoCajaRequest;
import com.maelespecieros.backend.dto.response.EgresoCajaResponse;

public interface EgresoCajaService {

    EgresoCajaResponse registrarEgreso(EgresoCajaRequest request);

    List<EgresoCajaResponse> listarEgresosCajaActual();

    List<EgresoCajaResponse> listarEgresosPorCaja(Long cajaId);

}

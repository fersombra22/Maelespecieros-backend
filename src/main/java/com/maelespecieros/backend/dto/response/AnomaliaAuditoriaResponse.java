package com.maelespecieros.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomaliaAuditoriaResponse {
    private String entidad;
    private String identificador;
    private String camposAfectados;
    private String mecanismoSeguridad;
    private String tipoAnomalia;
    private String descripcion;
}

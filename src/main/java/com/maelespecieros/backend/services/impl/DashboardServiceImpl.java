package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.response.DashboardResponse;
import com.maelespecieros.backend.entities.EstadoVenta;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public DashboardServiceImpl(
            ProductoRepository productoRepository,
            VentaRepository ventaRepository) {

        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public DashboardResponse obtenerDashboard() {

        long totalProductos = productoRepository.count();

        long productosActivos =
                productoRepository.countByActivoTrue();

        long stockBajo =
                productoRepository.countByStockLessThanEqualAndActivoTrue(5);

        long totalVentas =
                ventaRepository.countByEstado(
                        EstadoVenta.COMPLETADA);

        BigDecimal totalFacturado =
                ventaRepository.obtenerTotalFacturado();

        return new DashboardResponse(

                totalProductos,

                productosActivos,

                stockBajo,

                totalVentas,

                totalFacturado

        );

    }

}
package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.request.ProductoRequest;
import com.maelespecieros.backend.dto.response.ProductoResponse;
import com.maelespecieros.backend.entities.Categoria;
import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.maelespecieros.backend.repositories.CategoriaRepository;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.NumeradorService;
import com.maelespecieros.backend.services.ProductoService;
import com.maelespecieros.backend.services.InventoryService;

@Service
@Transactional
public class ProductoServiceImpl 
        implements ProductoService {

    private final ProductoRepository repository;

    private final CategoriaRepository categoriaRepository;

    private final NumeradorService numeradorService;

    private final BlockchainService blockchainService;
    
    private final InventoryService inventoryService;


    public ProductoServiceImpl(

            ProductoRepository repository,

            CategoriaRepository categoriaRepository,

            NumeradorService numeradorService,

            BlockchainService blockchainService,
            
            InventoryService inventoryService

    ){

        this.repository = repository;

        this.categoriaRepository = categoriaRepository;

        this.numeradorService = numeradorService;

        this.blockchainService = blockchainService;
        
        this.inventoryService = inventoryService;

    }


    private String getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
    }

    @Override
    public ProductoResponse crear(

            ProductoRequest request

    ){

        Categoria categoria =

                categoriaRepository.findById(
                        request.categoriaId()
                )

                .orElseThrow(
                        () ->
                        new ResourceNotFoundException( // <-- CORREGIDO
                                "Categoría no encontrada."
                        )
                );


        Producto producto =
                new Producto();


        producto.setCodigoProducto(

                numeradorService.generarCodigoProducto(
                        categoria.getPrefijo()
                )

        );


        producto.setNombre(
                request.nombre()
        );


        producto.setDescripcion(
                request.descripcion()
        );


        producto.setModelo(
                request.modelo()
        );


        producto.setPrecioEfectivo(
                request.precioEfectivo()
        );


        producto.setCosto(
                request.costo()
        );


        producto.setStock(
                request.stock()
        );


        producto.setStockMinimo(
                request.stockMinimo()
        );


        producto.setCategoria(
                categoria
        );


        producto.setActivo(true);


        repository.save(producto);

        // Generar movimiento de stock inicial (requerimiento de trazabilidad)
        if (producto.getStock() != null && producto.getStock() > 0) {
            Integer stockInicial = producto.getStock();
            producto.setStock(0);
            repository.save(producto);
            inventoryService.ingresarStock(producto, stockInicial, "Stock inicial de producto");
        }

        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "CREAR_PRODUCTO",
                "Producto creado: " + producto.getCodigoProducto(),
                "PRODUCTO",
                producto.getCodigoProducto(),
                producto
        );


        return convertir(producto);

    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(Pageable pageable){

        return repository
                .findByActivoTrue(pageable)
                .map(this::convertir);

    }


    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(

            Long id

    ){

        Producto producto =

                repository.findById(id)

                .orElseThrow(

                        () ->
                        new ResourceNotFoundException( // <-- CORREGIDO
                                "Producto no encontrado."
                        )

                );


        return convertir(producto);

    }


    @Override
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorCodigo(

            String codigo

    ){

        Producto producto =

                repository.findByCodigoProducto(codigo)

                .orElseThrow(

                        () ->
                        new ResourceNotFoundException( // <-- CORREGIDO
                                "Producto no encontrado."
                        )

                );


        return convertir(producto);

    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorNombre(

            String nombre,
            Pageable pageable

    ){

        return repository
                .findByNombreContainingIgnoreCase(nombre, pageable)

                .map(this::convertir);

    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listarPorCategoria(

            Long categoriaId,
            Pageable pageable

    ){

        return repository

                .findByCategoriaIdAndActivoTrue(
                        categoriaId, pageable
                )

                .map(this::convertir);

    }


    @Override
    public ProductoResponse actualizar(

            Long id,

            ProductoRequest request

    ){

        Producto producto =

                repository.findById(id)

                .orElseThrow(

                        () ->
                        new ResourceNotFoundException( // <-- CORREGIDO
                                "Producto no encontrado."
                        )

                );

        Categoria categoria =
                categoriaRepository.findById(
                        request.categoriaId()
                )
                .orElseThrow(
                        () ->
                        new ResourceNotFoundException(
                                "Categoría no encontrada."
                        )
                );
        producto.setCategoria(categoria);

        producto.setNombre(
                request.nombre()
        );


        producto.setDescripcion(
                request.descripcion()
        );


        producto.setModelo(
                request.modelo()
        );


        producto.setPrecioEfectivo(
                request.precioEfectivo()
        );


        producto.setCosto(
                request.costo()
        );


        producto.setStock(
                request.stock()
        );


        producto.setStockMinimo(
                request.stockMinimo()
        );


        repository.save(producto);


        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ACTUALIZAR_PRODUCTO",
                "Producto actualizado: " + producto.getCodigoProducto(),
                "PRODUCTO",
                producto.getCodigoProducto(),
                producto
        );


        return convertir(producto);

    }


    @Override
    public void desactivar(

            Long id

    ){

        Producto producto =

                repository.findById(id)

                .orElseThrow(

                        () ->
                        new ResourceNotFoundException( // <-- CORREGIDO
                                "Producto no encontrado."
                        )

                );


        producto.setActivo(false);


        repository.save(producto);


        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "DESACTIVAR_PRODUCTO",
                "Producto desactivado: " + producto.getCodigoProducto(),
                "PRODUCTO",
                producto.getCodigoProducto(),
                producto
        );

    }


    @Override
    public void activar(

            Long id

    ){

        Producto producto =

                repository.findById(id)

                .orElseThrow(

                        () ->
                        new ResourceNotFoundException( // <-- CORREGIDO
                                "Producto no encontrado."
                        )

                );


        producto.setActivo(true);


        repository.save(producto);


        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ACTIVAR_PRODUCTO",
                "Producto activado: " + producto.getCodigoProducto(),
                "PRODUCTO",
                producto.getCodigoProducto(),
                producto
        );

    }


    private ProductoResponse convertir(

            Producto producto

    ){

        return new ProductoResponse(

                producto.getId(),

                producto.getCodigoProducto(),

                producto.getNombre(),

                producto.getDescripcion(),

                producto.getModelo(),

                producto.getPrecioEfectivo(),

                producto.getCosto(),

                producto.getStock(),

                producto.getStockMinimo(),

                producto.getCategoria().getId(),

                producto.getCategoria().getNombre(),

                producto.getActivo()

        );

    }

}
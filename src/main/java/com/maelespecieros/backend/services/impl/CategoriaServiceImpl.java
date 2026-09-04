package com.maelespecieros.backend.services.impl;


import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.dto.request.CategoriaRequest;
import com.maelespecieros.backend.dto.response.CategoriaResponse;
import com.maelespecieros.backend.entities.Categoria;
import com.maelespecieros.backend.exceptions.DuplicateResourceException;
import com.maelespecieros.backend.exceptions.ResourceNotFoundException;
import com.maelespecieros.backend.repositories.CategoriaRepository;
import com.maelespecieros.backend.services.BlockchainService;
import com.maelespecieros.backend.services.CategoriaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;



@Service
@Transactional
public class CategoriaServiceImpl 
        implements CategoriaService {



    private final CategoriaRepository repository;

    private final BlockchainService blockchainService;



    public CategoriaServiceImpl(

            CategoriaRepository repository,

            BlockchainService blockchainService

    ){

        this.repository = repository;

        this.blockchainService = blockchainService;

    }

    private String getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
    }

    @Override
    public CategoriaResponse crear(

            CategoriaRequest request

    ){



        if(repository.existsByNombre(request.nombre())){


            throw new DuplicateResourceException(
                    "La categoría ya existe."
            );


        }



        if(repository.existsByPrefijo(request.prefijo())){


            throw new DuplicateResourceException(
                    "El prefijo ya existe."
            );


        }





        Categoria categoria = new Categoria();



        categoria.setNombre(
                request.nombre()
        );


        categoria.setDescripcion(
                request.descripcion()
        );


        categoria.setPrefijo(
                request.prefijo().toUpperCase()
        );


        categoria.setUltimoNumero(0);


        categoria.setActivo(true);




        repository.save(categoria);





        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "CREAR_CATEGORIA",
                "Categoría creada: " + categoria.getNombre(),
                "CATEGORIA",
                categoria.getId().toString(),
                categoria
        );




        return convertir(categoria);


    }









    @Override
    public CategoriaResponse actualizar(

            Long id,

            CategoriaRequest request

    ){



        Categoria categoria =

                repository.findById(id)

                .orElseThrow(

                        () -> new ResourceNotFoundException(
                                "Categoría no encontrada."
                        )

                );





        if(!categoria.getNombre()
                .equals(request.nombre())

                &&

           repository.existsByNombre(request.nombre())){


            throw new DuplicateResourceException(
                    "El nombre ya existe."
            );


        }






        if(!categoria.getPrefijo()
                .equals(request.prefijo())

                &&

           repository.existsByPrefijo(request.prefijo())){


            throw new DuplicateResourceException(
                    "El prefijo ya existe."
            );


        }






        categoria.setNombre(
                request.nombre()
        );


        categoria.setDescripcion(
                request.descripcion()
        );


        categoria.setPrefijo(
                request.prefijo().toUpperCase()
        );




        repository.save(categoria);





        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ACTUALIZAR_CATEGORIA",
                "Categoría actualizada: " + categoria.getNombre(),
                "CATEGORIA",
                categoria.getId().toString(),
                categoria
        );




        return convertir(categoria);


    }









    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(

            Pageable pageable

    ){



        return repository

                .findAll(pageable)

                .map(this::convertir);


    }









    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas(){



        return repository

                .findAllByOrderByNombreAsc()

                .stream()

                .map(this::convertir)

                .toList();


    }









    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(

            Long id

    ){



        return repository

                .findById(id)

                .map(this::convertir)

                .orElseThrow(

                        () -> new ResourceNotFoundException(
                                "Categoría no encontrada."
                        )

                );


    }









    @Override
    public void desactivar(

            Long id

    ){


        Categoria categoria = obtenerEntidad(id);



        categoria.setActivo(false);



        repository.save(categoria);





        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "DESACTIVAR_CATEGORIA",
                "Categoría desactivada: " + categoria.getNombre(),
                "CATEGORIA",
                categoria.getId().toString(),
                categoria
        );



    }









    @Override
    public void activar(

            Long id

    ){


        Categoria categoria = obtenerEntidad(id);



        categoria.setActivo(true);



        repository.save(categoria);





        blockchainService.registrarBloque(
                getUsuarioAutenticado(),
                "ACTIVAR_CATEGORIA",
                "Categoría activada: " + categoria.getNombre(),
                "CATEGORIA",
                categoria.getId().toString(),
                categoria
        );


    }










    private Categoria obtenerEntidad(

            Long id

    ){


        return repository.findById(id)

                .orElseThrow(

                        () -> new ResourceNotFoundException(
                                "Categoría no encontrada."
                        )

                );


    }








    private CategoriaResponse convertir(

            Categoria categoria

    ){



        return new CategoriaResponse(

                categoria.getId(),

                categoria.getNombre(),

                categoria.getDescripcion(),

                categoria.getPrefijo(),

                categoria.getUltimoNumero(),

                categoria.getActivo()

        );


    }



}
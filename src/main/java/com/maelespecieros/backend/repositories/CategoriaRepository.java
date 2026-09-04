package com.maelespecieros.backend.repositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maelespecieros.backend.entities.Categoria;



@Repository
public interface CategoriaRepository 
        extends JpaRepository<Categoria, Long> {



    Optional<Categoria> findByNombre(
            String nombre
    );



    Optional<Categoria> findByPrefijo(
            String prefijo
    );



    boolean existsByNombre(
            String nombre
    );



    boolean existsByPrefijo(
            String prefijo
    );



    List<Categoria> findAllByOrderByNombreAsc();



    List<Categoria> findByActivoTrueOrderByNombreAsc();



}
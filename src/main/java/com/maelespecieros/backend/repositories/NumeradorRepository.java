package com.maelespecieros.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maelespecieros.backend.entities.Numerador;

public interface NumeradorRepository extends JpaRepository<Numerador, Long> {

    Optional<Numerador> findByTipo(String tipo);

}
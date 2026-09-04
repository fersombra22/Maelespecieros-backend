package com.maelespecieros.backend.services.impl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.maelespecieros.backend.entities.Numerador;
import com.maelespecieros.backend.repositories.NumeradorRepository;
import com.maelespecieros.backend.services.NumeradorService;



@Service
@Transactional
public class NumeradorServiceImpl 
        implements NumeradorService {



    private final NumeradorRepository repository;



    public NumeradorServiceImpl(
            NumeradorRepository repository
    ){

        this.repository = repository;

    }







    @Override
    public String generarCodigoProducto(
            String prefijo
    ){


        Numerador numerador =

                repository.findByTipo(prefijo)

                .orElseGet(() -> {


                    Numerador n = new Numerador();


                    n.setTipo(prefijo);


                    n.setUltimoNumero(0);


                    return repository.save(n);


                });





        int nuevoNumero =

                numerador.getUltimoNumero() + 1;



        numerador.setUltimoNumero(
                nuevoNumero
        );



        repository.save(numerador);






        return String.format(

                "%s-%05d",

                prefijo,

                nuevoNumero

        );


    }









    @Override
    public String generarNumeroVenta(){


        Numerador numerador =

                repository.findByTipo("VENTA")

                .orElseGet(() -> {


                    Numerador n = new Numerador();


                    n.setTipo("VENTA");


                    n.setUltimoNumero(0);


                    return repository.save(n);


                });






        int nuevoNumero =

                numerador.getUltimoNumero() + 1;




        numerador.setUltimoNumero(
                nuevoNumero
        );



        repository.save(numerador);






        return String.format(

                "VTA-%06d",

                nuevoNumero

        );


    }



}
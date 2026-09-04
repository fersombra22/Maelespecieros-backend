package com.maelespecieros.backend.exceptions;


public class DuplicateResourceException 
        extends BusinessException {


    public DuplicateResourceException(
            String message
    ){

        super(message);

    }



}
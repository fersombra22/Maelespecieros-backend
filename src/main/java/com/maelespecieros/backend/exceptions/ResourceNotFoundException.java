package com.maelespecieros.backend.exceptions;


public class ResourceNotFoundException 
        extends BusinessException {


    public ResourceNotFoundException(
            String message
    ){

        super(message);

    }



}
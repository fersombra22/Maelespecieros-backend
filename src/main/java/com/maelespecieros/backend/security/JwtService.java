package com.maelespecieros.backend.security;


import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.maelespecieros.backend.entities.Rol;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;



@Service
public class JwtService {



    private final SecretKey key;


    private final long expiration;





    public JwtService(

            @Value("${jwt.secret}")
            String secret,


            @Value("${jwt.expiration}")
            long expiration

    ) {



        if(secret == null || secret.length() < 32){

            throw new IllegalArgumentException(
                    "jwt.secret debe tener mínimo 32 caracteres"
            );

        }



        this.key = Keys.hmacShaKeyFor(

                secret.getBytes(StandardCharsets.UTF_8)

        );



        this.expiration = expiration;


    }









    public String generarToken(

            String username,

            Rol rol

    ) {



        Date ahora = new Date();



        Date vencimiento = new Date(

                ahora.getTime()
                +
                expiration

        );




        return Jwts.builder()

                .subject(username)


                .claim(
                        "rol",
                        rol.name()
                )


                .issuedAt(ahora)


                .expiration(vencimiento)


                .signWith(key)


                .compact();


    }









    public String extraerUsername(

            String token

    ){


        return obtenerClaims(token)
                .getSubject();


    }









    public String extraerRol(

            String token

    ){


        return obtenerClaims(token)
                .get(
                        "rol",
                        String.class
                );


    }









    public boolean validarToken(

            String token

    ){


        try {


            obtenerClaims(token);


            return true;



        }catch(Exception e){


            return false;


        }


    }









    private Claims obtenerClaims(

            String token

    ){


        return Jwts.parser()


                .verifyWith(key)


                .build()


                .parseSignedClaims(token)


                .getPayload();


    }



}
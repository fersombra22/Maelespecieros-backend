package com.maelespecieros.backend.security;


import java.util.List;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



@Configuration
@EnableMethodSecurity
public class SecurityConfig {



    private final JwtAuthenticationFilter jwtAuthenticationFilter;




    public SecurityConfig(

            JwtAuthenticationFilter jwtAuthenticationFilter

    ){

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;

    }









    @Bean
    public SecurityFilterChain securityFilterChain(

            HttpSecurity http

    ) throws Exception {



        http


        .csrf(

                csrf -> csrf.disable()

        )



        .cors(

                cors -> cors.configurationSource(
                        corsConfigurationSource()
                )

        )



        .sessionManagement(

                session ->

                session.sessionCreationPolicy(

                        SessionCreationPolicy.STATELESS

                )

        )





        .authorizeHttpRequests(auth -> auth



                /*
                 * Login público
                 */
                .requestMatchers(
                        "/api/auth/**"
                )
                .permitAll()





                /*
                 * Recursos Angular
                 */
                .requestMatchers(

                        "/",
                        "/index.html",
                        "/assets/**",
                        "/media/**",
                        "/*.js",
                        "/*.css"

                )
                .permitAll()






                /*
                 * Blockchain
                 * Solo SUPER_ADMIN
                 */
                .requestMatchers(

                        "/api/blockchain/**"

                )
                .hasRole(

                        "SUPER_ADMIN"

                )







                .requestMatchers(
                        HttpMethod.GET,
                        "/api/categorias/**"
                )
                .authenticated()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/ventas/**"
                )
                .hasAnyRole(
                        "SUPER_ADMIN",
                        "ADMIN"
                )

                /*
                 * Restringir por roles
                 */
                .requestMatchers(
                        "/api/usuarios/**",
                        "/api/auditoria/**"
                )
                .hasRole("SUPER_ADMIN")

                .requestMatchers(
                        "/api/categorias/**",
                        "/api/movimientos/**",
                        "/api/reportes/**"
                )
                .hasAnyRole(
                        "SUPER_ADMIN",
                        "ADMIN"
                )







                /*
                 * Todo lo demás de API
                 */
                .requestMatchers(

                        "/api/**"

                )
                .authenticated()






                .anyRequest()

                .permitAll()



        )







        .addFilterBefore(

                jwtAuthenticationFilter,

                UsernamePasswordAuthenticationFilter.class

        );





        return http.build();


    }









    @Bean
    public CorsConfigurationSource corsConfigurationSource(){



        CorsConfiguration configuration =

                new CorsConfiguration();





        configuration.setAllowedOrigins(

                List.of(

                        "http://localhost:4200"

                )

        );





        configuration.setAllowedMethods(

                List.of(

                        HttpMethod.GET.name(),

                        HttpMethod.POST.name(),

                        HttpMethod.PUT.name(),

                        HttpMethod.DELETE.name(),

                        HttpMethod.PATCH.name(),

                        HttpMethod.OPTIONS.name()

                )

        );






        configuration.setAllowedHeaders(

                List.of(

                        "*"

                )

        );







        configuration.setAllowCredentials(

                true

        );







        UrlBasedCorsConfigurationSource source =

                new UrlBasedCorsConfigurationSource();





        source.registerCorsConfiguration(

                "/**",

                configuration

        );






        return source;


    }



}
package com.maelespecieros.backend.services;


import com.maelespecieros.backend.dto.request.CambiarPasswordRequest;
import com.maelespecieros.backend.dto.request.LoginRequest;
import com.maelespecieros.backend.dto.response.LoginResponse;


public interface AuthService {


    LoginResponse login(LoginRequest request);


    void cambiarPassword(CambiarPasswordRequest request);


}
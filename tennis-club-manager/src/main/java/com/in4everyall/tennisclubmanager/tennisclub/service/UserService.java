package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse signUp(SignUpRequest req);
    UserResponse login(LoginRequest req);
    UserResponse findByLicenseNumber(String licenseNumber);
    UserResponse findByEmail(String email);
    List<UserResponse> findByRole(String role);
}

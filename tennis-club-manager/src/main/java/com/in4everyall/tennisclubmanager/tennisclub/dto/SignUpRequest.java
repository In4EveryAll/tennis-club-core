package com.in4everyall.tennisclubmanager.tennisclub.dto;

public record SignUpRequest(
        String firstName,
        String lastName,
        String birthDate,
        String email,
        String password,
        String confirmPassword,
        String phone,
        String licenseNumber  // Opcional - si no viene, se genera automáticamente
) {}

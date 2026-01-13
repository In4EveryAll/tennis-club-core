package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.LoginRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.UserMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.validator.SignUpValidator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SignUpValidator signUpValidator;

    @Override
    public UserResponse signUp(SignUpRequest req) {
        signUpValidator.validateSignUpForm(req);
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        UserEntity userEntity = userMapper.toEntity(req);
        // Hashear la contraseña en backend
        userEntity.setPasswordHash(passwordEncoder.encode(req.password()));
        // Asignar role ALUMNO automáticamente (ignorar cualquier role recibido)
        userEntity.setRole(Role.ALUMNO);
        // Generar license_number único si no viene o está vacío
        if (req.licenseNumber() == null || req.licenseNumber().isBlank()) {
            userEntity.setLicenseNumber(generateLicenseNumber());
        } else {
            // Validar que el licenseNumber no exista
            if (userRepository.existsByLicenseNumber(req.licenseNumber())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "License number already exists");
            }
            userEntity.setLicenseNumber(req.licenseNumber());
        }
        UserEntity savedUser = userRepository.save(userEntity);
        // Generar token para el nuevo usuario
        String token = jwtService.generateToken(savedUser.getEmail());
        return userMapper.toResponse(savedUser, token);
    }
    
    private String generateLicenseNumber() {
        // Generar un número de licencia único basado en timestamp
        return "LIC-" + System.currentTimeMillis();
    }

    @Override
    public UserResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail());


        return userMapper.toResponse(user,token);
    }

    public UserResponse findByLicenseNumber(String licenseNumber) {
        UserEntity user = userRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse findByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> findByRole(String role) {
        if (role == null || role.isBlank()) {
            return userRepository.findAll().stream()
                    .map(userMapper::toResponse)
                    .toList();
        }
        Role roleEnum = Role.valueOf(role.toUpperCase());
        return userRepository.findByRole(roleEnum).stream()
                .map(userMapper::toResponse)
                .toList();
    }
}

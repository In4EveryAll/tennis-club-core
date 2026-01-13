package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    
    // Búsqueda case-insensitive (normaliza a lowercase)
    @org.springframework.data.jpa.repository.Query("SELECT u FROM UserEntity u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))")
    Optional<UserEntity> findByEmailIgnoreCase(@Param("email") String email);
    
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<UserEntity> findByLicenseNumber(String licenseNumber);
    Optional<UserEntity> findByEmailAndLicenseNumber(String email, String licenseNumber);
    List<UserEntity> findByRole(Role role);
}

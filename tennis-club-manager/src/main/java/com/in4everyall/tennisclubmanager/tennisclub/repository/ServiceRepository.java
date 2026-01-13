package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    Optional<ServiceEntity> findByCode(String code);
    List<ServiceEntity> findByIsActive(Boolean isActive);
    List<ServiceEntity> findByServiceType(ServiceEntity.ServiceType serviceType);
}










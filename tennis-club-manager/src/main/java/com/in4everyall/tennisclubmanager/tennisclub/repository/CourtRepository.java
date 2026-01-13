package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourtRepository extends JpaRepository<CourtEntity, UUID> {
    List<CourtEntity> findByIsActiveTrue();
    List<CourtEntity> findByIsActive(Boolean isActive);
}






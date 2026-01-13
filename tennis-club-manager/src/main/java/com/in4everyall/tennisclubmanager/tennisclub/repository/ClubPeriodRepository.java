package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.ClubPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubPeriodRepository extends JpaRepository<ClubPeriodEntity, UUID> {
    List<ClubPeriodEntity> findByIsActive(Boolean isActive);
    List<ClubPeriodEntity> findByPeriodType(ClubPeriodEntity.PeriodType periodType);
    
    @org.springframework.data.jpa.repository.Query(
        "SELECT p FROM ClubPeriodEntity p WHERE p.startDate <= :date AND p.endDate >= :date AND p.isActive = true"
    )
    Optional<ClubPeriodEntity> findActivePeriodByDate(LocalDate date);
}










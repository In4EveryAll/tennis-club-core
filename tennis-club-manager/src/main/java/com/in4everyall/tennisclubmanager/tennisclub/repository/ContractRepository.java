package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<ContractEntity, UUID> {
    Optional<ContractEntity> findByContractNumber(String contractNumber);
    List<ContractEntity> findByUserEmail(String userEmail);
    List<ContractEntity> findByServiceId(UUID serviceId);
    List<ContractEntity> findByStatus(ContractEntity.ContractStatus status);
    
    @Query("SELECT c FROM ContractEntity c WHERE c.user.email = :userEmail AND c.status = 'ACTIVE'")
    List<ContractEntity> findActiveContractsByUserEmail(@Param("userEmail") String userEmail);
    
    @Query("SELECT c FROM ContractEntity c " +
           "WHERE c.service.id = :serviceId " +
           "AND c.status = 'ACTIVE' " +
           "AND c.startDate <= :eventDate " +
           "AND (c.endDate IS NULL OR c.endDate >= :eventDate)")
    List<ContractEntity> findActiveContractsByServiceAndDate(
            @Param("serviceId") UUID serviceId,
            @Param("eventDate") java.time.LocalDate eventDate
    );
    
    @Query("SELECT c FROM ContractEntity c " +
           "WHERE c.service.id = :serviceId " +
           "AND c.period.id = :periodId " +
           "AND c.status = 'ACTIVE'")
    List<ContractEntity> findActiveContractsByServiceAndPeriod(
            @Param("serviceId") UUID serviceId,
            @Param("periodId") UUID periodId
    );
}






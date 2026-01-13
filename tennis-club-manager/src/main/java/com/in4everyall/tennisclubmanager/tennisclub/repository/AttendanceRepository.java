package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, UUID> {
    List<AttendanceEntity> findByEventId(UUID eventId);
    List<AttendanceEntity> findByUserEmail(String userEmail);
    List<AttendanceEntity> findByContractId(UUID contractId);
    
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.event.id = :eventId AND a.attendanceStatus = :status")
    Integer countByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") String status);
    
    Optional<AttendanceEntity> findByEventIdAndUserEmail(UUID eventId, String userEmail);
}









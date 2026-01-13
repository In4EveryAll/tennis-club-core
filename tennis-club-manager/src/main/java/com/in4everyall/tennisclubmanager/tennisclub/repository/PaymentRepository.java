package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    
    // Métodos usando userEmail (nuevo sistema)
    List<PaymentEntity> findByUserEmail(String userEmail);
    
    List<PaymentEntity> findByUserEmailOrderByPaymentDateDesc(String userEmail);
    
    List<PaymentEntity> findByUserEmailAndStatus(String userEmail, PaymentStatus status);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.userEmail = :userEmail AND p.status = :status ORDER BY p.paymentDate DESC")
    List<PaymentEntity> findPendingPaymentsByUser(@Param("userEmail") String userEmail, @Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.userEmail = :userEmail AND p.status = 'PAID'")
    java.math.BigDecimal getTotalPaidByUser(@Param("userEmail") String userEmail);
    
    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.userEmail = :userEmail AND p.status = 'PENDING'")
    java.math.BigDecimal getTotalPendingByUser(@Param("userEmail") String userEmail);
    
    // Métodos generales
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<PaymentEntity> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Métodos por contrato
    List<PaymentEntity> findByContractId(UUID contractId);
    
    // Método legacy para suscripciones (deprecated - subscription ya no existe en PaymentEntity)
    @Deprecated
    default List<PaymentEntity> findBySubscription_Id(UUID subscriptionId) {
        return List.of();
    }
    
    // Métodos legacy (deprecated - no funcionan porque player ya no existe)
    // Se mantienen por compatibilidad pero retornan listas vacías
    @Deprecated
    default List<PaymentEntity> findByPlayer_LicenseNumber(String licenseNumber) {
        return List.of();
    }
    
    @Deprecated
    default List<PaymentEntity> findByPlayer_LicenseNumberOrderByPaymentDateDesc(String licenseNumber) {
        return List.of();
    }
    
    @Deprecated
    default List<PaymentEntity> findByPaymentType(PaymentType paymentType) {
        return List.of();
    }
    
    @Deprecated
    default List<PaymentEntity> findByPlayer_LicenseNumberAndStatus(String licenseNumber, PaymentStatus status) {
        return List.of();
    }
    
    @Deprecated
    default List<PaymentEntity> findPendingPaymentsByPlayer(String licenseNumber, PaymentStatus status) {
        return List.of();
    }
    
    @Deprecated
    default java.math.BigDecimal getTotalPaidByPlayer(String licenseNumber) {
        return java.math.BigDecimal.ZERO;
    }
    
    @Deprecated
    default java.math.BigDecimal getTotalPendingByPlayer(String licenseNumber) {
        return java.math.BigDecimal.ZERO;
    }
}





package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contracts")
public class ContractEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @Column(name = "contract_number", length = 50, nullable = false, unique = true)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_email", referencedColumnName = "email")
    private UserEntity monitor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private ContractStatus status = ContractStatus.PENDING;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "signed_date")
    private LocalDate signedDate;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "total_classes")
    private Integer totalClasses;

    @Column(name = "classes_used", nullable = false)
    @Builder.Default
    private Integer classesUsed = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private ClubPeriodEntity period;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = false;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum ContractStatus {
        ACTIVE, SUSPENDED, CANCELLED, EXPIRED, PENDING
    }
}



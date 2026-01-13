package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "services")
public class ServiceEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ServiceType serviceType;

    @Column(name = "day_of_week", length = 20)
    private String dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "min_capacity")
    private Integer minCapacity;

    @Column(name = "classes_in_package")
    private Integer classesInPackage;

    @Column(name = "package_validity_days")
    private Integer packageValidityDays;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum ServiceType {
        QUARTERLY_GROUP_CLASS,
        INDIVIDUAL_CLASS_PACKAGE,
        SINGLE_INDIVIDUAL_CLASS
    }
}



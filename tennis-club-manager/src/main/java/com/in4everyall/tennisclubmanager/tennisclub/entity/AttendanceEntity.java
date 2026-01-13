package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendances")
public class AttendanceEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEventEntity event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AttendanceStatus attendanceStatus = AttendanceStatus.PRESENT;

    @Column(name = "arrival_time")
    private Instant arrivalTime;

    @Column(name = "departure_time")
    private Instant departureTime;

    @Column(name = "notes", length = 500)
    private String notes;

    public enum AttendanceStatus {
        PRESENT, ABSENT, EXCUSED, LATE
    }
}









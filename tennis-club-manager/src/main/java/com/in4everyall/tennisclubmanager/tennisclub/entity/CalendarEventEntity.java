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
@Table(name = "calendar_events")
public class CalendarEventEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EventType eventType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_datetime", nullable = false)
    private Instant startDatetime;

    @Column(name = "end_datetime")
    private Instant endDatetime;

    @Column(name = "is_all_day", nullable = false)
    @Builder.Default
    private Boolean isAllDay = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_email", referencedColumnName = "email")
    private UserEntity monitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private ClubPeriodEntity period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id")
    private CourtEntity court;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", referencedColumnName = "email")
    private UserEntity user;

    @Column(name = "participants_count")
    private Integer participantsCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private EventStatus status = EventStatus.SCHEDULED;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum EventType {
        CLASS, RESERVATION, TOURNAMENT, LEAGUE, HOLIDAY, CLOSURE, SPECIAL_EVENT
    }

    public enum EventStatus {
        SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED
    }
}



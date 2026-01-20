package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;
import java.util.UUID;

public record CalendarEventResponse(
        UUID id,
        String eventType,
        String title,
        String description,
        Instant startDateTime,
        Instant endDateTime,
        String serviceName,
        String monitorName,
        String monitorEmail,
        String monitor_email,
        Integer confirmedCount,
        Integer capacity,
        Boolean userHasConfirmed,
        String status,
        UserInfo user,  // Información del usuario (solo para ADMIN cuando es RESERVATION)
        String userName,  // Nombre completo calculado (solo para ADMIN cuando es RESERVATION)
        String userEmail  // Email del usuario (solo para ADMIN cuando es RESERVATION)
) {}









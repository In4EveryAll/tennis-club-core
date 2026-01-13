package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;

public record AvailabilitySlotResponse(
        Instant start,
        Instant end,
        Boolean available,
        Boolean blockedByClass,  // true si está bloqueado por una clase, false si está bloqueado por una reserva, null si está disponible
        ReservationInfo reservation  // Información de la reserva (solo para ADMIN cuando el slot está ocupado por una reserva)
) {}





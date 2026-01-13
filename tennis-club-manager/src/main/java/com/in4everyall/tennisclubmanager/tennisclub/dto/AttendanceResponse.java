package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        UUID eventId,
        String userId,
        UUID contractId,
        String status,
        Instant arrivalTime,
        Instant departureTime,
        String notes
) {}









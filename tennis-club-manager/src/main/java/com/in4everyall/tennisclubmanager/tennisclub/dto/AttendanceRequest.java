package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.UUID;

public record AttendanceRequest(
        UUID eventId,
        String userId,  // email del usuario
        UUID contractId,
        String status  // "PRESENT" | "ABSENT" | "EXCUSED" | "LATE"
) {}









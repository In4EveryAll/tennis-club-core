package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID courtId,
        String courtName,
        Instant startDatetime,
        Instant endDatetime,
        String status,
        String userEmail,
        UserInfo user,  // Información completa del usuario
        String userName  // Nombre completo calculado (firstName + " " + lastName)
) {}






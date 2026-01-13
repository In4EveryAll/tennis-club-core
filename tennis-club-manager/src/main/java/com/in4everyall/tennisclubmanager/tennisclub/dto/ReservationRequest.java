package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationRequest(
        UUID courtId,
        Instant startDatetime,
        Instant endDatetime
) {}






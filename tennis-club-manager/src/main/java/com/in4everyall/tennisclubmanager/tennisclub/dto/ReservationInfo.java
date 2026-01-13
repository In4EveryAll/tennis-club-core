package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.UUID;

public record ReservationInfo(
        UUID id,
        String userName,
        String userEmail
) {}


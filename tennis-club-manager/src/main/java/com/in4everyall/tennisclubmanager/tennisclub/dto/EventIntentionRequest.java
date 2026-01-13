package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.UUID;

public record EventIntentionRequest(
        UUID eventId,
        String userId,  // email del usuario
        String intention  // "GOING" | "NOT_GOING"
) {}









package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.UUID;

public record EventIntentionResponse(
        UUID id,
        UUID eventId,
        String userId,
        String intention,
        String status
) {}









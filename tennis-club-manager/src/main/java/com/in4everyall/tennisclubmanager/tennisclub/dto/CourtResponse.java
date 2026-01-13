package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.UUID;

public record CourtResponse(
        UUID id,
        String name,
        String imageUrl,
        String surface,  // "CLAY", "HARD", "GRASS"
        Boolean isActive
) {}






package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClubPeriodResponse(
        UUID id,
        String name,
        String periodType,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive,
        String description
) {}









package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String code,
        String name,
        String description,
        String serviceType,
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal basePrice,
        String currency,
        Integer maxCapacity,
        Integer minCapacity,
        Integer classesInPackage,
        Integer packageValidityDays,
        Boolean isActive
) {}


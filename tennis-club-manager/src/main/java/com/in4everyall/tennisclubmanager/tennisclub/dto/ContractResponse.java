package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        String contractNumber,
        String userEmail,
        String userName,
        UUID serviceId,
        String serviceName,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        String currency,
        Integer totalClasses,
        Integer classesUsed,
        Integer classesRemaining,
        UUID periodId,
        String periodName,
        Boolean autoRenew,
        String notes
) {}





package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractRequest(
        String userEmail,      // Email del alumno
        UUID serviceId,        // ID del servicio
        UUID periodId,         // ID del periodo (si es trimestral)
        BigDecimal price,      // Precio del contrato
        LocalDate startDate,   // Fecha de inicio
        LocalDate endDate,     // Fecha de fin (opcional)
        Integer totalClasses,  // Total de clases (para bonos)
        String notes           // Notas adicionales
) {}





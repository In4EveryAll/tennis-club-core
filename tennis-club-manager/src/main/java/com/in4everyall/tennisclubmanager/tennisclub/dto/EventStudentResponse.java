package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.UUID;

public record EventStudentResponse(
        String userId,              // email del usuario
        String name,                // nombre completo del usuario
        String email,               // email del usuario
        UUID contractId,            // ID del contrato
        String contractType,        // "TRIMESTRAL" | "BONO"
        Boolean hasPendingPayments,  // si tiene pagos pendientes
        String intention,           // "GOING" | "NOT_GOING" | null
        String attendance           // "PRESENT" | "ABSENT" | null
) {}






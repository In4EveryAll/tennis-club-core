package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CalendarEventWithStudentsResponse(
        UUID id,
        String serviceName,
        Instant date,                    // startDatetime del evento
        Instant startTime,               // startDatetime
        Integer capacity,                // maxCapacity del servicio
        UUID periodId,                   // ID del periodo/trimestre
        String periodName,               // Nombre del periodo/trimestre
        List<EventStudentResponse> students,
        Integer confirmedCount,          // cantidad de estudiantes con intención GOING
        Integer totalStudents            // total de estudiantes inscritos
) {}





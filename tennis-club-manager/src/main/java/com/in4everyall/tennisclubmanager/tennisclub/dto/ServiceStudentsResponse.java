package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.List;
import java.util.UUID;

public record ServiceStudentsResponse(
        UUID serviceId,
        String serviceName,
        UUID periodId,
        String periodName,
        Integer totalStudents,
        Integer confirmedCount,
        Integer capacity,
        List<EventStudentResponse> students
) {}



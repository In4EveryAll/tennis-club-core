package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventResponse;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;

import java.util.List;
import java.util.UUID;

public interface CalendarEventService {
    List<CalendarEventResponse> getEventsByMonth(String month, Role userRole);
    CalendarEventResponse getEventById(UUID eventId, Role userRole);
    List<CalendarEventResponse> getEventsByDateRange(java.time.Instant start, java.time.Instant end, Role userRole);
    com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventWithStudentsResponse getEventWithStudents(UUID eventId);
}





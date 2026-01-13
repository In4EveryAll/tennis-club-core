package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.List;

public record CalendarMonthResponse(
        Integer year,
        Integer month,
        List<CalendarEventResponse> events,
        List<ClubPeriodResponse> periods
) {}









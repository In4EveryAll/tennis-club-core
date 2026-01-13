package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardTodayResponse(
        LocalDate date,
        DashboardSummary summary,
        List<CalendarEventResponse> events,
        String clubStatus  // 'OPEN' | 'CLOSED' | 'HOLIDAY'
) {
    public record DashboardSummary(
            Integer classesToday,
            Integer pendingPayments,
            Integer activeContracts
    ) {}
}









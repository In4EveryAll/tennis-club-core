package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AvailabilitySlotResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.CourtResponse;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CourtService {
    List<CourtResponse> getActiveCourts();
    List<AvailabilitySlotResponse> getCourtAvailability(UUID courtId, LocalDate date, Role userRole);
}






package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClubPeriodResponse;

import java.util.List;
import java.util.UUID;

public interface ClubPeriodService {
    List<ClubPeriodResponse> getAllPeriods();
    List<ClubPeriodResponse> getActivePeriods();
    ClubPeriodResponse getPeriodById(UUID id);
}









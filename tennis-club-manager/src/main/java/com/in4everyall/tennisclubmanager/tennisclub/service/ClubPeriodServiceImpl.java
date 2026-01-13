package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClubPeriodResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ClubPeriodEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ClubPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubPeriodServiceImpl implements ClubPeriodService {

    private final ClubPeriodRepository periodRepository;

    @Override
    public List<ClubPeriodResponse> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ClubPeriodResponse> getActivePeriods() {
        return periodRepository.findByIsActive(true).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ClubPeriodResponse getPeriodById(UUID id) {
        ClubPeriodEntity period = periodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Periodo no encontrado"));
        return toResponse(period);
    }

    private ClubPeriodResponse toResponse(ClubPeriodEntity period) {
        return new ClubPeriodResponse(
                period.getId(),
                period.getName(),
                period.getPeriodType() != null ? period.getPeriodType().name() : null,
                period.getStartDate(),
                period.getEndDate(),
                period.getIsActive(),
                period.getDescription()
        );
    }
}









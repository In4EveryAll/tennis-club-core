package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ClubPeriodResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.ClubPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/club-periods")
@RequiredArgsConstructor
public class ClubPeriodController {

    private final ClubPeriodService clubPeriodService;

    @GetMapping
    public ResponseEntity<List<ClubPeriodResponse>> getPeriods(
            @RequestParam(required = false) Boolean active
    ) {
        List<ClubPeriodResponse> periods;
        if (active != null && active) {
            periods = clubPeriodService.getActivePeriods();
        } else {
            periods = clubPeriodService.getAllPeriods();
        }
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubPeriodResponse> getPeriodById(@PathVariable UUID id) {
        ClubPeriodResponse period = clubPeriodService.getPeriodById(id);
        return ResponseEntity.ok(period);
    }
}









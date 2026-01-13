package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AvailabilitySlotResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.CourtResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CourtResponse>> getActiveCourts() {
        return ResponseEntity.ok(courtService.getActiveCourts());
    }

    @GetMapping("/{courtId}/availability")
    public ResponseEntity<List<AvailabilitySlotResponse>> getCourtAvailability(
            @PathVariable UUID courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        // Obtener el rol del usuario autenticado
        String userEmail = authentication.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElse(null);
        Role userRole = (user != null) ? user.getRole() : Role.ALUMNO;
        
        return ResponseEntity.ok(courtService.getCourtAvailability(courtId, date, userRole));
    }
}






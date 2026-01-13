package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ReservationResponse response = reservationService.createReservation(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(reservationService.getMyReservations(userEmail));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable UUID eventId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        reservationService.cancelReservation(eventId, userEmail, isAdmin);
        return ResponseEntity.noContent().build();
    }
}






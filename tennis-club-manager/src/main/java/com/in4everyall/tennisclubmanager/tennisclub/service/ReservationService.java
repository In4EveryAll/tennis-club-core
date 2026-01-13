package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationResponse;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(ReservationRequest request, String userEmail);
    void cancelReservation(UUID eventId, String userEmail, boolean isAdmin);
    List<ReservationResponse> getMyReservations(String userEmail);
}






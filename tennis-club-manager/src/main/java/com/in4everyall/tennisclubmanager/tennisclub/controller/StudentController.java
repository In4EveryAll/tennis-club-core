package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.service.CalendarEventService;
import com.in4everyall.tennisclubmanager.tennisclub.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final ContractService contractService;
    private final CalendarEventService calendarEventService;
    private final UserRepository userRepository;

    /**
     * Obtener contratos del alumno autenticado
     */
    @GetMapping("/contracts")
    public ResponseEntity<List<ContractResponse>> getMyContracts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        List<ContractResponse> contracts = contractService.getContractsByUserEmail(userEmail);
        return ResponseEntity.ok(contracts);
    }

    /**
     * Obtener contratos activos del alumno autenticado
     */
    @GetMapping("/contracts/active")
    public ResponseEntity<List<ContractResponse>> getMyActiveContracts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        List<ContractResponse> allContracts = contractService.getContractsByUserEmail(userEmail);
        List<ContractResponse> activeContracts = allContracts.stream()
                .filter(c -> "ACTIVE".equals(c.status()))
                .toList();
        return ResponseEntity.ok(activeContracts);
    }

    /**
     * Obtener eventos del calendario para el alumno autenticado
     */
    @GetMapping("/calendar-events")
    public ResponseEntity<List<CalendarEventResponse>> getMyCalendarEvents(
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (month != null && !month.isBlank()) {
            // Obtener el rol del usuario autenticado
            String userEmail = userDetails.getUsername();
            UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
            Role userRole = (user != null) ? user.getRole() : Role.ALUMNO;
            
            // Obtener eventos del mes
            // Nota: Por ahora devolvemos todos los eventos del mes
            // En el futuro se pueden filtrar por contratos del usuario si CalendarEventResponse incluye contractId
            List<CalendarEventResponse> events = calendarEventService.getEventsByMonth(month, userRole);
            return ResponseEntity.ok(events);
        }
        
        // Si no se especifica mes, devolver lista vacía
        return ResponseEntity.ok(List.of());
    }
}


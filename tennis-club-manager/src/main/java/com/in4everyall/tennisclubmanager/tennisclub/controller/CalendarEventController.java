package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import com.in4everyall.tennisclubmanager.tennisclub.service.CalendarEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/calendar-events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> getEventsByMonth(
            @RequestParam String month,
            Authentication authentication
    ) {
        try {
            log.debug("Obteniendo eventos del calendario para el mes: {}", month);

            // Validar parámetro
            if (month == null || month.isBlank()) {
                log.warn("Parámetro 'month' vacío o null");
                return ResponseEntity.badRequest().build();
            }

            // Obtener el rol del usuario autenticado
            String userEmail = authentication.getName();
            UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
            Role userRole = (user != null) ? user.getRole() : Role.ALUMNO;

            // Obtener eventos
            List<CalendarEventResponse> events = calendarEventService.getEventsByMonth(month, userRole);

            // ✅ SIEMPRE devolver array, incluso si está vacío
            if (events == null) {
                log.warn("El servicio devolvió null para el mes: {}. Devolviendo lista vacía.", month);
                return ResponseEntity.ok(new ArrayList<>());
            }

            log.debug("Devolviendo {} eventos para el mes: {}", events.size(), month);
            return ResponseEntity.ok(events);

        } catch (ResponseStatusException e) {
            // Errores de validación (400 Bad Request)
            log.warn("Error de validación al obtener eventos: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Error real del servidor - loguear pero devolver lista vacía para evitar 500
            log.error("Error inesperado al obtener eventos del calendario para el mes: {}", month, e);
            // En lugar de devolver 500, devolver lista vacía
            // Esto evita que el frontend falle cuando no hay datos
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<CalendarEventResponse> getEventById(
            @PathVariable UUID eventId,
            Authentication authentication
    ) {
        try {
            // Obtener el rol del usuario autenticado
            String userEmail = authentication.getName();
            UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
            Role userRole = (user != null) ? user.getRole() : Role.ALUMNO;
            
            CalendarEventResponse event = calendarEventService.getEventById(eventId, userRole);
            return ResponseEntity.ok(event);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener evento: {}", eventId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener el evento");
        }
    }

    @GetMapping("/{eventId}/students")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventWithStudentsResponse> getEventWithStudents(@PathVariable UUID eventId) {
        try {
            log.debug("Obteniendo evento con estudiantes: {}", eventId);
            com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventWithStudentsResponse event = calendarEventService.getEventWithStudents(eventId);
            return ResponseEntity.ok(event);
        } catch (ResponseStatusException e) {
            log.warn("Error al obtener evento con estudiantes: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener evento con estudiantes: {}", eventId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener el evento con estudiantes");
        }
    }
}


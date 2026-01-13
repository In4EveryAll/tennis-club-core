package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserInfo;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CourtEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CalendarEventRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CourtRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final CalendarEventRepository calendarEventRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String userEmail) {
        // Validar que la pista existe y está activa
        CourtEntity court = courtRepository.findById(request.courtId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));
        
        if (!court.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa");
        }

        // Validar y normalizar el email (trim para evitar problemas de espacios)
        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "El email del usuario no puede estar vacío. Email recibido del JWT: " + userEmail);
        }
        
        String trimmedEmail = userEmail.trim();
        
        // Buscar el usuario (primero con búsqueda case-insensitive, luego exacta)
        UserEntity user = userRepository.findByEmailIgnoreCase(trimmedEmail)
                .orElseGet(() -> {
                    // Si no se encuentra con búsqueda case-insensitive, intentar búsqueda exacta
                    return userRepository.findByEmail(trimmedEmail)
                            .orElse(null);
                });
        
        if (user == null) {
            // Mensaje de error mejorado con información de depuración
            String errorMessage = String.format(
                    "Usuario no encontrado. Email del JWT: '%s' (normalizado: '%s'). " +
                    "Verifica que: " +
                    "1) El usuario con email '%s' existe en la tabla 'users' de la base de datos. " +
                    "2) El email en el JWT coincide exactamente con el email almacenado (sin espacios, case-sensitive). " +
                    "3) La migración V2.1.1 se ejecutó correctamente para crear el usuario admin.",
                    userEmail, trimmedEmail, trimmedEmail);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
        }

        // Validar que endDatetime es posterior a startDatetime
        if (request.endDatetime().isBefore(request.startDatetime()) || 
            request.endDatetime().equals(request.startDatetime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // Validar duración de la reserva
        Duration duration = Duration.between(request.startDatetime(), request.endDatetime());
        long durationMinutes = duration.toMinutes();
        
        // 1. Validar que la duración no exceda 120 minutos (2 horas)
        if (durationMinutes > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "No se permiten reservas de más de 2 horas");
        }
        
        // 2. Validar que la duración sea múltiplo de 30 minutos
        if (durationMinutes % 30 != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La duración de la reserva debe ser múltiplo de 30 minutos (30, 60, 90 o 120 minutos)");
        }

        // 3. Validar que todos los slots intermedios estén disponibles
        // Generar todos los slots de 30 minutos que cubre la reserva
        // Ejemplo: 18:00-19:30 → [18:00-18:30, 18:30-19:00, 19:00-19:30]
        java.time.Instant currentSlotStart = request.startDatetime();
        java.util.List<java.time.Instant> slotStarts = new java.util.ArrayList<>();
        
        while (currentSlotStart.isBefore(request.endDatetime())) {
            slotStarts.add(currentSlotStart);
            currentSlotStart = currentSlotStart.plus(30, ChronoUnit.MINUTES);
        }
        
        // Validar cada slot de 30 minutos
        for (java.time.Instant slotStart : slotStarts) {
            java.time.Instant slotEnd = slotStart.plus(30, ChronoUnit.MINUTES);
            
            // 3.1. Buscar reservas que solapen con este slot en esta pista específica
            List<CalendarEventEntity> overlappingReservations = calendarEventRepository.findOverlappingEvents(
                    request.courtId(),
                    slotStart,
                    slotEnd
            );
            
            // 3.2. Buscar clases que solapen con este slot (las clases bloquean todas las pistas)
            List<CalendarEventEntity> overlappingClasses = calendarEventRepository.findOverlappingClasses(
                    slotStart,
                    slotEnd
            );
            
            if (!overlappingReservations.isEmpty() || !overlappingClasses.isEmpty()) {
                String message = "El horario seleccionado no está disponible. ";
                if (!overlappingReservations.isEmpty()) {
                    message += "Hay reservas que solapan con este horario. ";
                }
                if (!overlappingClasses.isEmpty()) {
                    message += "Hay clases programadas que bloquean este horario.";
                }
                throw new ResponseStatusException(HttpStatus.CONFLICT, message.trim());
            }
        }

        // Crear el evento de reserva
        CalendarEventEntity reservation = CalendarEventEntity.builder()
                .eventType(CalendarEventEntity.EventType.RESERVATION)
                .title("Reserva de pista: " + court.getName())
                .description("Reserva de pista realizada por " + user.getEmail())
                .startDatetime(request.startDatetime())
                .endDatetime(request.endDatetime())
                .court(court)
                .user(user)
                .status(CalendarEventEntity.EventStatus.CONFIRMED)
                .isAllDay(false)
                .build();

        CalendarEventEntity saved = calendarEventRepository.save(reservation);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(String userEmail) {
        List<CalendarEventEntity> reservations = calendarEventRepository.findReservationsByUserEmail(userEmail);
        return reservations.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelReservation(UUID eventId, String userEmail, boolean isAdmin) {
        CalendarEventEntity reservation = calendarEventRepository.findByIdWithRelations(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        // Validar que es una reserva
        if (reservation.getEventType() != CalendarEventEntity.EventType.RESERVATION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El evento no es una reserva");
        }

        // Validar que solo el dueño o ADMIN puede cancelar
        if (!isAdmin && (reservation.getUser() == null || !reservation.getUser().getEmail().equals(userEmail))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Solo el dueño de la reserva o un administrador puede cancelarla");
        }

        // Cancelar la reserva
        reservation.setStatus(CalendarEventEntity.EventStatus.CANCELLED);
        reservation.setCancellationReason("Cancelada por " + (isAdmin ? "administrador" : "usuario"));
        calendarEventRepository.save(reservation);
    }

    private ReservationResponse toResponse(CalendarEventEntity reservation) {
        UserInfo userInfo = null;
        String userName = null;
        
        if (reservation.getUser() != null) {
            userInfo = new UserInfo(
                    reservation.getUser().getEmail(),
                    reservation.getUser().getFirstName(),
                    reservation.getUser().getLastName()
            );
            userName = reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName();
        }
        
        return new ReservationResponse(
                reservation.getId(),
                reservation.getCourt() != null ? reservation.getCourt().getId() : null,
                reservation.getCourt() != null ? reservation.getCourt().getName() : null,
                reservation.getStartDatetime(),
                reservation.getEndDatetime(),
                reservation.getStatus().name(),
                reservation.getUser() != null ? reservation.getUser().getEmail() : null,
                userInfo,
                userName
        );
    }
}





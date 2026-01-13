package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AvailabilitySlotResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.CourtResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ReservationInfo;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CourtEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CalendarEventRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CalendarEventRepository calendarEventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponse> getActiveCourts() {
        return courtRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getCourtAvailability(UUID courtId, LocalDate date, Role userRole) {
        // Verificar que la pista existe y está activa
        CourtEntity court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));
        
        if (!court.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pista no está activa");
        }

        // Generar slots cada 30 minutos entre 08:00 y 22:00
        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(22, 0);
        
        LocalTime current = startTime;
        while (current.isBefore(endTime)) {
            LocalTime slotEnd = current.plusMinutes(30);
            
            Instant slotStartInstant = date.atTime(current).atZone(ZoneId.systemDefault()).toInstant();
            Instant slotEndInstant = date.atTime(slotEnd).atZone(ZoneId.systemDefault()).toInstant();
            
            slots.add(new AvailabilitySlotResponse(slotStartInstant, slotEndInstant, true, null, null));
            current = slotEnd;
        }

        // Obtener eventos del día que bloquean la pista
        Instant dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        // 1. Buscar reservas en esta pista específica
        List<CalendarEventEntity> reservations = calendarEventRepository.findByCourtIdAndDateRange(
                courtId, dayStart, dayEnd
        );
        
        // 2. Buscar clases que bloquean todas las pistas (las clases no tienen court_id)
        // Primero buscar todas las clases del día, luego filtrar por solapamiento con los slots
        List<CalendarEventEntity> allClassesOfDay = calendarEventRepository.findClassesByDateRange(
                dayStart, dayEnd
        );
        
        // Filtrar clases que realmente solapan con algún slot del día
        // Un slot solapa si: slotStart < eventEnd AND slotEnd > eventStart
        List<CalendarEventEntity> classes = new ArrayList<>();
        for (CalendarEventEntity cls : allClassesOfDay) {
            Instant eventStart = cls.getStartDatetime();
            Instant eventEnd = cls.getEndDatetime() != null 
                    ? cls.getEndDatetime() 
                    : eventStart.plusSeconds(1800); // 30 min por defecto
            
            // Verificar si la clase solapa con algún slot del día
            LocalTime slotTime = startTime;
            boolean overlaps = false;
            while (slotTime.isBefore(endTime) && !overlaps) {
                Instant slotStart = date.atTime(slotTime).atZone(ZoneId.systemDefault()).toInstant();
                Instant slotEnd = slotStart.plusSeconds(1800); // 30 minutos
                
                if (slotStart.isBefore(eventEnd) && slotEnd.isAfter(eventStart)) {
                    overlaps = true;
                }
                slotTime = slotTime.plusMinutes(30);
            }
            
            if (overlaps) {
                classes.add(cls);
            }
        }
        
        // Combinar ambos tipos de eventos
        List<CalendarEventEntity> events = new java.util.ArrayList<>();
        events.addAll(reservations);
        events.addAll(classes);

        // Crear mapas para rastrear qué slots están bloqueados por clases vs reservas
        // Key: Instant del slot, Value: true si es clase, false si es reserva
        java.util.Map<Instant, Boolean> blockedByClassMap = new java.util.HashMap<>();
        
        // Procesar eventos para identificar qué slots están bloqueados y por qué tipo
        for (CalendarEventEntity event : events) {
            if (event.getStatus() == CalendarEventEntity.EventStatus.CANCELLED) {
                continue;
            }
            
            Instant eventStart = event.getStartDatetime();
            Instant eventEnd = event.getEndDatetime() != null 
                    ? event.getEndDatetime() 
                    : eventStart.plusSeconds(1800); // 30 min por defecto
            
            // Determinar si es una clase o una reserva
            boolean isClass = event.getEventType() == CalendarEventEntity.EventType.CLASS;
            
            // Encontrar todos los slots que solapan con este evento
            LocalTime slotTime = startTime;
            while (slotTime.isBefore(endTime)) {
                Instant slotStart = date.atTime(slotTime).atZone(ZoneId.systemDefault()).toInstant();
                Instant slotEnd = slotStart.plusSeconds(1800); // 30 minutos
                
                // Verificar si el slot solapa con el evento
                if (slotStart.isBefore(eventEnd) && slotEnd.isAfter(eventStart)) {
                    // Si ya está bloqueado por una clase, mantenerlo como clase (las clases tienen prioridad)
                    // Si no está bloqueado o está bloqueado por reserva, actualizar según el evento actual
                    if (!blockedByClassMap.containsKey(slotStart) || isClass) {
                        blockedByClassMap.put(slotStart, isClass);
                    }
                }
                
                slotTime = slotTime.plusMinutes(30);
            }
        }

        // Crear un mapa de reservas por slot para incluir información del usuario (solo para ADMIN)
        java.util.Map<Instant, CalendarEventEntity> reservationBySlotMap = new java.util.HashMap<>();
        if (userRole == Role.ADMIN) {
            for (CalendarEventEntity event : reservations) {
                if (event.getStatus() == CalendarEventEntity.EventStatus.CANCELLED) {
                    continue;
                }
                
                Instant eventStart = event.getStartDatetime();
                Instant eventEnd = event.getEndDatetime() != null 
                        ? event.getEndDatetime() 
                        : eventStart.plusSeconds(1800);
                
                // Encontrar todos los slots que solapan con esta reserva
                LocalTime slotTime = startTime;
                while (slotTime.isBefore(endTime)) {
                    Instant slotStart = date.atTime(slotTime).atZone(ZoneId.systemDefault()).toInstant();
                    Instant slotEnd = slotStart.plusSeconds(1800);
                    
                    if (slotStart.isBefore(eventEnd) && slotEnd.isAfter(eventStart)) {
                        // Solo guardar la primera reserva encontrada para este slot
                        if (!reservationBySlotMap.containsKey(slotStart)) {
                            reservationBySlotMap.put(slotStart, event);
                        }
                    }
                    
                    slotTime = slotTime.plusMinutes(30);
                }
            }
        }
        
        // Marcar slots ocupados como no disponibles y agregar información de blockedByClass y reserva
        return slots.stream()
                .map(slot -> {
                    boolean isBlocked = blockedByClassMap.containsKey(slot.start());
                    boolean available = !isBlocked;
                    Boolean blockedByClass = isBlocked ? blockedByClassMap.get(slot.start()) : null;
                    
                    // Incluir información de reserva solo si:
                    // 1. El usuario es ADMIN
                    // 2. El slot está bloqueado por una reserva (no por una clase)
                    // 3. Existe información de reserva para este slot
                    ReservationInfo reservation = null;
                    if (userRole == Role.ADMIN && blockedByClass != null && !blockedByClass && reservationBySlotMap.containsKey(slot.start())) {
                        CalendarEventEntity reservationEvent = reservationBySlotMap.get(slot.start());
                        if (reservationEvent.getUser() != null) {
                            String userName = reservationEvent.getUser().getFirstName() + " " + reservationEvent.getUser().getLastName();
                            reservation = new ReservationInfo(
                                    reservationEvent.getId(),
                                    userName,
                                    reservationEvent.getUser().getEmail()
                            );
                        }
                    }
                    
                    return new AvailabilitySlotResponse(
                            slot.start(), 
                            slot.end(), 
                            available,
                            blockedByClass,
                            reservation
                    );
                })
                .toList();
    }

    private CourtResponse toResponse(CourtEntity court) {
        return new CourtResponse(
                court.getId(),
                court.getName(),
                court.getImageUrl(),
                court.getSurface().name(),
                court.getIsActive()
        );
    }
}





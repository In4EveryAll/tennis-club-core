package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.EventIntentionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.EventIntentionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CalendarEventRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventIntentionServiceImpl implements EventIntentionService {

    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;
    // Nota: Las intenciones se pueden almacenar en una tabla separada o usar attendances con status especial
    // Por ahora, usaremos una implementación simple que actualiza el participants_count del evento

    @Override
    @Transactional
    public EventIntentionResponse createOrUpdateIntention(EventIntentionRequest request) {
        CalendarEventEntity event = calendarEventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        UserEntity user = userRepository.findByEmail(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Validar intención
        if (!request.intention().equalsIgnoreCase("GOING") && !request.intention().equalsIgnoreCase("NOT_GOING")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intención debe ser GOING o NOT_GOING");
        }

        // Por ahora, actualizamos el participants_count del evento
        // En una implementación completa, esto debería estar en una tabla separada de intenciones
        if (request.intention().equalsIgnoreCase("GOING")) {
            if (event.getParticipantsCount() == null) {
                event.setParticipantsCount(0);
            }
            event.setParticipantsCount(event.getParticipantsCount() + 1);
        }

        calendarEventRepository.save(event);

        return new EventIntentionResponse(
                UUID.randomUUID(), // En una implementación completa, esto sería el ID de la intención
                event.getId(),
                user.getEmail(),
                request.intention().toUpperCase(),
                "ACTIVE"
        );
    }

    @Override
    public List<EventIntentionResponse> getIntentionsByEventId(UUID eventId) {
        // Implementación simplificada - en producción debería consultar una tabla de intenciones
        return List.of();
    }

    @Override
    public List<EventIntentionResponse> getIntentionsByUserId(String userId) {
        // Implementación simplificada - en producción debería consultar una tabla de intenciones
        return List.of();
    }
}









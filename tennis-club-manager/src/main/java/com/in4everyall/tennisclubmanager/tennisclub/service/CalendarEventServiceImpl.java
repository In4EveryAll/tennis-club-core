package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventWithStudentsResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.EventStudentResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.UserInfo;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.entity.AttendanceEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ContractEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.repository.AttendanceRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CalendarEventRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ContractRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final AttendanceRepository attendanceRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEventsByMonth(String month, Role userRole) {
        // Validar que el parámetro no sea null o vacío
        if (month == null || month.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro 'month' es obligatorio. Use formato YYYY-MM (ej: 2026-01)");
        }

        // Parsear el mes (formato: "2026-01")
        YearMonth yearMonth;
        try {
            // Intentar parsear directamente con YearMonth.parse (formato ISO: YYYY-MM)
            yearMonth = YearMonth.parse(month);
        } catch (java.time.format.DateTimeParseException e) {
            // Si falla el parseo ISO, intentar parseo manual más flexible
            try {
                String trimmedMonth = month.trim();
                String[] parts = trimmedMonth.split("-");
                if (parts.length != 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Formato de mes inválido. Use YYYY-MM (ej: 2026-01)");
                }
                int year = Integer.parseInt(parts[0].trim());
                int monthValue = Integer.parseInt(parts[1].trim());
                if (monthValue < 1 || monthValue > 12) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "El mes debe estar entre 1 y 12");
                }
                if (year < 2000 || year > 2100) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "El año debe estar entre 2000 y 2100");
                }
                yearMonth = YearMonth.of(year, monthValue);
            } catch (NumberFormatException nfe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Formato de mes inválido. Use YYYY-MM (ej: 2026-01)");
            } catch (java.time.DateTimeException dte) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Fecha inválida. Use YYYY-MM (ej: 2026-01)");
            }
        }

        try {
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            // Consultar eventos - Spring Data JPA siempre devuelve lista vacía si no hay resultados
            List<CalendarEventEntity> events = calendarEventRepository.findByStartDatetimeBetween(startInstant, endInstant);

            // ✅ SIEMPRE devolver lista (vacía o con datos), nunca null
            if (events == null || events.isEmpty()) {
                return new java.util.ArrayList<>();
            }

            // Mapear a DTOs de forma segura
            return events.stream()
                    .map(event -> toResponse(event, userRole))
                    .filter(response -> response != null) // Filtrar respuestas null por si acaso
                    .toList();
        } catch (Exception e) {
            // Log del error para debugging
            log.error("Error al obtener eventos del calendario para el mes: {}", month, e);
            // En caso de error, devolver lista vacía en lugar de lanzar excepción
            // Esto evita error 500 cuando hay problemas menores
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarEventResponse getEventById(UUID eventId, Role userRole) {
        CalendarEventEntity event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        return toResponse(event, userRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEventsByDateRange(Instant start, Instant end, Role userRole) {
        try {
            List<CalendarEventEntity> events = calendarEventRepository.findByStartDatetimeBetween(start, end);
            
            // ✅ SIEMPRE devolver lista (vacía o con datos), nunca null
            if (events == null || events.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            return events.stream()
                    .map(event -> toResponse(event, userRole))
                    .filter(response -> response != null) // Filtrar respuestas null por si acaso
                    .toList();
        } catch (Exception e) {
            log.error("Error al obtener eventos por rango de fechas: {} - {}", start, end, e);
            return new java.util.ArrayList<>();
        }
    }

    private CalendarEventResponse toResponse(CalendarEventEntity event, Role userRole) {
        // Validar que el evento no sea null
        if (event == null) {
            return null;
        }

        try {
            // Validar que los campos requeridos no sean null
            if (event.getId() == null || event.getEventType() == null || event.getStatus() == null) {
                log.warn("Evento con campos requeridos null: {}", event.getId());
                return null; // Evento inválido, filtrar
            }

            // Obtener nombre del servicio (con fetch join ya está cargado)
            String serviceName = null;
            Integer capacity = null;
            if (event.getService() != null) {
                serviceName = event.getService().getName();
                capacity = event.getService().getMaxCapacity();
            }

            // Obtener nombre del monitor (con fetch join ya está cargado)
            String monitorName = null;
            if (event.getMonitor() != null) {
                String firstName = event.getMonitor().getFirstName() != null ? event.getMonitor().getFirstName() : "";
                String lastName = event.getMonitor().getLastName() != null ? event.getMonitor().getLastName() : "";
                monitorName = (firstName + " " + lastName).trim();
                if (monitorName.isEmpty()) {
                    monitorName = null;
                }
            }

            // Contar asistencias confirmadas (PRESENT)
            // Usar participants_count si está disponible, sino contar desde attendances
            Integer confirmedCount = event.getParticipantsCount();
            if (confirmedCount == null) {
                try {
                    List<AttendanceEntity> attendances = attendanceRepository.findByEventId(event.getId());
                    confirmedCount = (int) attendances.stream()
                            .filter(a -> a != null && a.getAttendanceStatus() == AttendanceEntity.AttendanceStatus.PRESENT)
                            .count();
                } catch (Exception e) {
                    log.warn("Error al contar asistencias para evento {}: {}", event.getId(), e.getMessage());
                    confirmedCount = 0;
                }
            }

            // Incluir información del usuario solo si:
            // 1. El usuario es ADMIN
            // 2. El evento es una RESERVATION
            // 3. El evento tiene un usuario asociado
            UserInfo userInfo = null;
            String userName = null;
            String userEmail = null;
            
            if (userRole == Role.ADMIN && 
                event.getEventType() == CalendarEventEntity.EventType.RESERVATION && 
                event.getUser() != null) {
                userInfo = new UserInfo(
                        event.getUser().getEmail(),
                        event.getUser().getFirstName(),
                        event.getUser().getLastName()
                );
                userName = event.getUser().getFirstName() + " " + event.getUser().getLastName();
                userEmail = event.getUser().getEmail();
            }
            
            return new CalendarEventResponse(
                    event.getId(),
                    event.getEventType().name(),
                    event.getTitle() != null ? event.getTitle() : "",
                    event.getDescription(),
                    event.getStartDatetime(),
                    event.getEndDatetime(),
                    serviceName,
                    monitorName,
                    confirmedCount != null ? confirmedCount : 0,
                    capacity,
                    false, // userHasConfirmed se calcula en el frontend o con el usuario autenticado
                    event.getStatus().name(),
                    userInfo,
                    userName,
                    userEmail
            );
        } catch (Exception e) {
            // Si hay cualquier error al mapear, loguear y devolver null (será filtrado)
            log.error("Error al mapear evento a DTO: {}", event.getId(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarEventWithStudentsResponse getEventWithStudents(UUID eventId) {
        // Obtener el evento
        CalendarEventEntity event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        // Validar que el evento tenga un servicio asociado
        if (event.getService() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El evento no tiene un servicio asociado");
        }

        // Convertir la fecha del evento a LocalDate para filtrar contratos
        LocalDate eventDate = event.getStartDatetime()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Buscar contratos activos del servicio que incluyan la fecha del evento
        List<ContractEntity> activeContracts = contractRepository.findActiveContractsByServiceAndDate(
                event.getService().getId(),
                eventDate
        );

        // Obtener todas las asistencias del evento
        List<AttendanceEntity> attendances = attendanceRepository.findByEventId(eventId);

        // Crear un mapa de asistencias por userEmail para acceso rápido
        java.util.Map<String, AttendanceEntity> attendanceMap = attendances.stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getUser().getEmail(),
                        a -> a,
                        (existing, replacement) -> existing // Si hay duplicados, mantener el primero
                ));

        // Mapear contratos a estudiantes
        List<EventStudentResponse> students = activeContracts.stream()
                .map(contract -> {
                    UserEntity user = contract.getUser();
                    
                    // Obtener nombre completo
                    String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                    String lastName = user.getLastName() != null ? user.getLastName() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    if (fullName.isEmpty()) {
                        fullName = user.getEmail(); // Fallback al email si no hay nombre
                    }

                    // Determinar tipo de contrato
                    String contractType = "BONO";
                    if (contract.getService() != null && 
                        contract.getService().getServiceType() == com.in4everyall.tennisclubmanager.tennisclub.entity.ServiceEntity.ServiceType.QUARTERLY_GROUP_CLASS) {
                        contractType = "TRIMESTRAL";
                    }

                    // Verificar pagos pendientes
                    boolean hasPendingPayments = !paymentRepository.findByUserEmailAndStatus(
                            user.getEmail(),
                            PaymentStatus.PENDING
                    ).isEmpty();

                    // Obtener intención (por ahora null - se implementará con tabla event_intentions más adelante)
                    String intention = null;

                    // Obtener asistencia desde la tabla attendances
                    String attendanceStatus = null;
                    AttendanceEntity attendance = attendanceMap.get(user.getEmail());
                    if (attendance != null) {
                        switch (attendance.getAttendanceStatus()) {
                            case PRESENT:
                                attendanceStatus = "PRESENT";
                                break;
                            case ABSENT:
                                attendanceStatus = "ABSENT";
                                break;
                            case EXCUSED:
                            case LATE:
                                // Para estos estados, también podemos devolver el estado específico
                                attendanceStatus = attendance.getAttendanceStatus().name();
                                break;
                            default:
                                attendanceStatus = null;
                        }
                    }

                    return new EventStudentResponse(
                            user.getEmail(),
                            fullName,
                            user.getEmail(),
                            contract.getId(),
                            contractType,
                            hasPendingPayments,
                            intention,
                            attendanceStatus
                    );
                })
                .collect(java.util.stream.Collectors.toList());

        // Contar confirmados (por ahora, contamos los que tienen asistencia PRESENT)
        // En el futuro, esto debería contar las intenciones GOING
        int confirmedCount = (int) students.stream()
                .filter(s -> "PRESENT".equals(s.attendance()))
                .count();

        // Obtener capacidad del servicio
        Integer capacity = event.getService().getMaxCapacity();

        // Obtener información del periodo si está asociado
        UUID periodId = null;
        String periodName = null;
        if (event.getPeriod() != null) {
            periodId = event.getPeriod().getId();
            periodName = event.getPeriod().getName();
        } else if (!activeContracts.isEmpty() && activeContracts.get(0).getPeriod() != null) {
            // Si el evento no tiene periodo pero los contratos sí, usar el del primer contrato
            periodId = activeContracts.get(0).getPeriod().getId();
            periodName = activeContracts.get(0).getPeriod().getName();
        }

        return new CalendarEventWithStudentsResponse(
                event.getId(),
                event.getService().getName(),
                event.getStartDatetime(),
                event.getStartDatetime(),
                capacity,
                periodId,
                periodName,
                students,
                confirmedCount,
                students.size()
        );
    }
}


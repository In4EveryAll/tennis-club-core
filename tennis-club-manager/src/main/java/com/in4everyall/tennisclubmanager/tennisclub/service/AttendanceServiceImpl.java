package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AttendanceRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.AttendanceResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.AttendanceEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ContractEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.AttendanceRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CalendarEventRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ContractRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public AttendanceResponse createOrUpdateAttendance(AttendanceRequest request) {
        CalendarEventEntity event = calendarEventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        UserEntity user = userRepository.findByEmail(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        ContractEntity contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));

        // Buscar asistencia existente
        AttendanceEntity attendance = attendanceRepository.findByEventIdAndUserEmail(request.eventId(), request.userId())
                .orElse(null);

        if (attendance == null) {
            // Crear nueva asistencia
            AttendanceEntity.AttendanceStatus status = parseStatus(request.status());
            attendance = AttendanceEntity.builder()
                    .event(event)
                    .user(user)
                    .contract(contract)
                    .attendanceStatus(status)
                    .arrivalTime(status == AttendanceEntity.AttendanceStatus.PRESENT ? Instant.now() : null)
                    .build();
        } else {
            // Actualizar asistencia existente
            AttendanceEntity.AttendanceStatus status = parseStatus(request.status());
            attendance.setAttendanceStatus(status);
            if (status == AttendanceEntity.AttendanceStatus.PRESENT && attendance.getArrivalTime() == null) {
                attendance.setArrivalTime(Instant.now());
            }
        }

        AttendanceEntity saved = attendanceRepository.save(attendance);

        // Si es PRESENT y el contrato es un bono, actualizar classesUsed
        if (saved.getAttendanceStatus() == AttendanceEntity.AttendanceStatus.PRESENT 
                && contract.getTotalClasses() != null) {
            updateContractClassesUsed(contract);
        }

        return toResponse(saved);
    }

    private void updateContractClassesUsed(ContractEntity contract) {
        if (contract.getClassesUsed() == null) {
            contract.setClassesUsed(0);
        }
        contract.setClassesUsed(contract.getClassesUsed() + 1);
        // classesRemaining se calcula en el DTO, no se almacena en la BD
        contractRepository.save(contract);
    }

    private AttendanceEntity.AttendanceStatus parseStatus(String status) {
        try {
            return AttendanceEntity.AttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de asistencia inválido: " + status);
        }
    }

    @Override
    public List<AttendanceResponse> getAttendancesByEventId(UUID eventId) {
        return attendanceRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AttendanceResponse> getAttendancesByUserId(String userId) {
        return attendanceRepository.findByUserEmail(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AttendanceResponse toResponse(AttendanceEntity attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getEvent().getId(),
                attendance.getUser().getEmail(),
                attendance.getContract().getId(),
                attendance.getAttendanceStatus().name(),
                attendance.getArrivalTime(),
                attendance.getDepartureTime(),
                attendance.getNotes()
        );
    }
}


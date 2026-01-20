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
        if (request == null
                || request.eventId() == null
                || request.contractId() == null
                || request.userId() == null || request.userId().isBlank()
                || request.status() == null || request.status().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body");
        }

        AttendanceEntity.AttendanceStatus newStatus = parseStatus(request.status());
        boolean isPresentOrLate = newStatus == AttendanceEntity.AttendanceStatus.PRESENT
                || newStatus == AttendanceEntity.AttendanceStatus.LATE;

        CalendarEventEntity event = calendarEventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        UserEntity user = userRepository.findByEmail(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ContractEntity contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));

        // Buscar asistencia existente (UPSERT por eventId + user_email)
        AttendanceEntity attendance = attendanceRepository.findByEventIdAndUserEmail(request.eventId(), request.userId())
                .orElse(null);

        AttendanceEntity.AttendanceStatus previousStatus = null;
        if (attendance == null) {
            // INSERT
            attendance = AttendanceEntity.builder()
                    .event(event)
                    .user(user)
                    .contract(contract)
                    .attendanceStatus(newStatus)
                    .arrivalTime(isPresentOrLate ? Instant.now() : null)
                    .build();
        } else {
            // UPDATE
            previousStatus = attendance.getAttendanceStatus();
            attendance.setContract(contract); // actualizar si cambió
            attendance.setAttendanceStatus(newStatus);
            // arrival_time: set NOW() solo si pasa a PRESENT/LATE y estaba NULL; si no, mantener
            if (isPresentOrLate && attendance.getArrivalTime() == null) {
                attendance.setArrivalTime(Instant.now());
            }
        }

        AttendanceEntity saved = attendanceRepository.save(attendance);

        // BONOS: incrementar classesUsed solo UNA vez por evento (cuando transiciona a PRESENT/LATE)
        if (contract.getTotalClasses() != null && isPresentOrLate) {
            boolean wasPresentOrLate = previousStatus == AttendanceEntity.AttendanceStatus.PRESENT
                    || previousStatus == AttendanceEntity.AttendanceStatus.LATE;
            boolean shouldIncrement = previousStatus == null || !wasPresentOrLate;
            if (shouldIncrement) {
                incrementContractClassesUsedIfAvailable(contract);
            }
        }

        return toResponse(saved);
    }

    private void incrementContractClassesUsedIfAvailable(ContractEntity contract) {
        Integer total = contract.getTotalClasses();
        if (total == null) return; // no es bono

        Integer used = contract.getClassesUsed();
        if (used == null) used = 0;

        int remaining = total - used;
        if (remaining <= 0) return;

        contract.setClassesUsed(used + 1);
        // classesRemaining se calcula en el DTO, no se almacena en la BD
        contractRepository.save(contract);
    }

    private AttendanceEntity.AttendanceStatus parseStatus(String status) {
        try {
            return AttendanceEntity.AttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid attendance status: " + status);
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


package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.*;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.Role;
import com.in4everyall.tennisclubmanager.tennisclub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ClubPeriodRepository periodRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        // Validar campos requeridos
        if (request.userEmail() == null || request.userEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email del usuario es obligatorio");
        }
        
        if (request.serviceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del servicio es obligatorio");
        }
        
        if (request.price() == null || request.price().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que cero");
        }
        
        if (request.startDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio es obligatoria");
        }
        
        // Validar que el usuario existe y es ALUMNO
        UserEntity user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        
        if (user.getRole() != Role.ALUMNO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden crear contratos para alumnos");
        }

        // Validar que el servicio existe y está activo
        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
        
        if (service.getIsActive() == null || !service.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El servicio no está activo");
        }

        // Validar periodo si es necesario
        ClubPeriodEntity period = null;
        if (request.periodId() != null) {
            period = periodRepository.findById(request.periodId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Periodo no encontrado"));
            
            if (period.getIsActive() == null || !period.getIsActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El periodo no está activo");
            }
        }
        
        // Validar que si es servicio trimestral, tiene periodo
        if (service.getServiceType() == ServiceEntity.ServiceType.QUARTERLY_GROUP_CLASS) {
            if (request.periodId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los servicios trimestrales requieren un periodo");
            }
            if (service.getDayOfWeek() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El servicio trimestral debe tener definido el día de la semana");
            }
        }
        
        // Validar que si es bono, tiene totalClasses
        if (service.getServiceType() == ServiceEntity.ServiceType.INDIVIDUAL_CLASS_PACKAGE) {
            if (request.totalClasses() == null || request.totalClasses() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los bonos requieren un número de clases mayor que cero");
            }
        }
        
        // Validar fechas
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // Crear contrato
        ContractEntity contract = ContractEntity.builder()
                .contractNumber(generateContractNumber())
                .user(user)
                .service(service)
                .status(ContractEntity.ContractStatus.ACTIVE)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .signedDate(LocalDate.now())
                .price(request.price())
                .currency("EUR")
                .totalClasses(request.totalClasses())
                .classesUsed(0)
                .period(period)
                .autoRenew(false)
                .notes(request.notes())
                .build();

        ContractEntity savedContract = contractRepository.save(contract);

        // Crear pago automáticamente asociado al contrato
        createPaymentForContract(savedContract);

        // Si es un contrato trimestral (QUARTERLY_GROUP_CLASS), generar calendar_events
        if (service.getServiceType() == ServiceEntity.ServiceType.QUARTERLY_GROUP_CLASS && period != null) {
            generateCalendarEventsForQuarterlyContract(savedContract, service, period);
        }

        return toResponse(savedContract);
    }

    private void createPaymentForContract(ContractEntity contract) {
        // El paymentNumber se genera automáticamente en @PrePersist
        LocalDate today = LocalDate.now();
        // La fecha de vencimiento debe ser >= fecha de pago (restricción ck_payment_due_date)
        // Si la fecha de inicio del contrato es anterior a hoy, usar hoy como fecha de vencimiento
        LocalDate dueDate = contract.getStartDate().isBefore(today) 
                ? today 
                : contract.getStartDate();
        
        PaymentEntity payment = PaymentEntity.builder()
                .contract(contract)
                .userEmail(contract.getUser().getEmail())
                .amount(contract.getPrice())
                .currency(contract.getCurrency())
                .paymentDate(today)
                .dueDate(dueDate)
                .status(PaymentStatus.PENDING)
                .build();
        
        paymentRepository.save(payment);
    }

    private void generateCalendarEventsForQuarterlyContract(
            ContractEntity contract,
            ServiceEntity service,
            ClubPeriodEntity period) {
        
        if (service.getDayOfWeek() == null || service.getStartTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El servicio debe tener día de la semana y hora de inicio para generar eventos"
            );
        }

        LocalDate currentDate = period.getStartDate();
        LocalDate endDate = period.getEndDate();
        DayOfWeek targetDayOfWeek = DayOfWeek.valueOf(service.getDayOfWeek().toUpperCase());
        
        List<CalendarEventEntity> events = new ArrayList<>();
        
        // Generar eventos para cada semana del periodo
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek() == targetDayOfWeek) {
                // Crear evento para este día
                LocalTime startTime = service.getStartTime();
                LocalTime endTime = service.getEndTime();
                
                if (endTime == null) {
                    // Si no hay endTime, usar 1.5 horas por defecto (duración típica de clase de tenis)
                    endTime = startTime.plusHours(1).plusMinutes(30);
                }
                
                Instant startDatetime = currentDate.atTime(startTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                
                Instant endDatetime = currentDate.atTime(endTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                
                CalendarEventEntity event = CalendarEventEntity.builder()
                        .eventType(CalendarEventEntity.EventType.CLASS)
                        .title(service.getName())
                        .description(service.getDescription())
                        .startDatetime(startDatetime)
                        .endDatetime(endDatetime)
                        .isAllDay(false)
                        .contract(contract)
                        .service(service)
                        .period(period)
                        .status(CalendarEventEntity.EventStatus.SCHEDULED)
                        .court(null)  // ✅ Asegurar explícitamente que las clases NO tienen court_id
                        .build();
                
                events.add(event);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        calendarEventRepository.saveAll(events);
    }

    private String generateContractNumber() {
        return "CT-" + System.currentTimeMillis();
    }

    private ContractResponse toResponse(ContractEntity contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getContractNumber(),
                contract.getUser().getEmail(),
                contract.getUser().getFirstName() + " " + contract.getUser().getLastName(),
                contract.getService().getId(),
                contract.getService().getName(),
                contract.getStatus().name(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getPrice(),
                contract.getCurrency(),
                contract.getTotalClasses(),
                contract.getClassesUsed(),
                // Calcular classesRemaining: total_classes - classes_used
                contract.getTotalClasses() != null ? contract.getTotalClasses() - contract.getClassesUsed() : null,
                contract.getPeriod() != null ? contract.getPeriod().getId() : null,
                contract.getPeriod() != null ? contract.getPeriod().getName() : null,
                contract.getAutoRenew(),
                contract.getNotes()
        );
    }

    @Override
    public ContractResponse getContractById(UUID id) {
        ContractEntity contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));
        return toResponse(contract);
    }

    @Override
    public List<ContractResponse> getContractsByUserEmail(String userEmail) {
        return contractRepository.findByUserEmail(userEmail).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ContractResponse> getActiveContracts() {
        return contractRepository.findByStatus(ContractEntity.ContractStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID id, java.util.Map<String, Object> updates) {
        ContractEntity contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));

        if (updates != null) {
            if (updates.containsKey("classesUsed")) {
                Integer classesUsed = (Integer) updates.get("classesUsed");
                contract.setClassesUsed(classesUsed);
                // classesRemaining se calcula en el DTO, no se almacena
            }
            if (updates.containsKey("status")) {
                String statusStr = (String) updates.get("status");
                try {
                    contract.setStatus(ContractEntity.ContractStatus.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + statusStr);
                }
            }
        }

        ContractEntity saved = contractRepository.save(contract);
        return toResponse(saved);
    }
}


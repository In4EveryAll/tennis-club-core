package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.EventStudentResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceStudentsResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ClubPeriodEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ContractEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ServiceEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ClubPeriodRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ContractRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ContractRepository contractRepository;
    private final ClubPeriodRepository periodRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ServiceResponse> getActiveServices() {
        return serviceRepository.findByIsActive(true).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ServiceResponse getServiceById(UUID id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
        return toResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStudentsResponse getStudentsByServiceAndPeriod(UUID serviceId, UUID periodId) {
        // Validar servicio
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        // Validar periodo
        ClubPeriodEntity period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Periodo no encontrado"));

        // Buscar contratos activos del servicio en el periodo
        List<ContractEntity> contracts = contractRepository.findActiveContractsByServiceAndPeriod(serviceId, periodId);

        // Mapear contratos a estudiantes
        List<EventStudentResponse> students = contracts.stream()
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
                        contract.getService().getServiceType() == ServiceEntity.ServiceType.QUARTERLY_GROUP_CLASS) {
                        contractType = "TRIMESTRAL";
                    }

                    // Verificar pagos pendientes
                    boolean hasPendingPayments = !paymentRepository.findByUserEmailAndStatus(
                            user.getEmail(),
                            PaymentStatus.PENDING
                    ).isEmpty();

                    // Por ahora, no hay intención (se implementará con tabla event_intentions)
                    String intention = null;

                    // Por ahora, no hay asistencia específica (se obtiene por evento)
                    String attendance = null;

                    return new EventStudentResponse(
                            user.getEmail(),
                            fullName,
                            user.getEmail(),
                            contract.getId(),
                            contractType,
                            hasPendingPayments,
                            intention,
                            attendance
                    );
                })
                .collect(Collectors.toList());

        // Contar confirmados (por ahora 0, se actualizará cuando se implementen intenciones)
        int confirmedCount = 0;

        // Obtener capacidad del servicio
        Integer capacity = service.getMaxCapacity();

        return new ServiceStudentsResponse(
                service.getId(),
                service.getName(),
                period.getId(),
                period.getName(),
                students.size(),
                confirmedCount,
                capacity,
                students
        );
    }

    private ServiceResponse toResponse(ServiceEntity service) {
        return new ServiceResponse(
                service.getId(),
                service.getCode(),
                service.getName(),
                service.getDescription(),
                service.getServiceType() != null ? service.getServiceType().name() : null,
                service.getDayOfWeek(),
                service.getStartTime(),
                service.getEndTime(),
                service.getBasePrice(),
                service.getCurrency(),
                service.getMaxCapacity(),
                service.getMinCapacity(),
                service.getClassesInPackage(),
                service.getPackageValidityDays(),
                service.getIsActive()
        );
    }
}


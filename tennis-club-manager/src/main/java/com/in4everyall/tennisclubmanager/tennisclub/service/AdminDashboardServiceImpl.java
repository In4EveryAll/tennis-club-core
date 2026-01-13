package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.CalendarEventResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.DashboardTodayResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.ContractEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.repository.ContractRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final CalendarEventService calendarEventService;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public DashboardTodayResponse getTodayDashboard() {
        LocalDate today = LocalDate.now();
        
        // Obtener eventos de hoy
        Instant startOfDay = today.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        // Este método es llamado desde el dashboard del admin, así que siempre es ADMIN
        List<CalendarEventResponse> eventsToday = calendarEventService.getEventsByDateRange(startOfDay, endOfDay, com.in4everyall.tennisclubmanager.tennisclub.enums.Role.ADMIN);
        
        // Contar clases de hoy (solo eventos de tipo CLASS)
        Integer classesToday = (int) eventsToday.stream()
                .filter(e -> "CLASS".equals(e.eventType()))
                .count();
        
        // Contar pagos pendientes
        Integer pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING).size();
        
        // Contar contratos activos
        Integer activeContracts = contractRepository.findByStatus(ContractEntity.ContractStatus.ACTIVE).size();
        
        // Determinar estado del club (simplificado: siempre OPEN por ahora)
        String clubStatus = "OPEN";
        
        return new DashboardTodayResponse(
                today,
                new DashboardTodayResponse.DashboardSummary(
                        classesToday,
                        pendingPayments,
                        activeContracts
                ),
                eventsToday,
                clubStatus
        );
    }
}


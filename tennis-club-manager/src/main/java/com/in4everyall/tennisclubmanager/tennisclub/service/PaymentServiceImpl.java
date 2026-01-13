package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.UserEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import com.in4everyall.tennisclubmanager.tennisclub.mapper.PaymentMapper;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import com.in4everyall.tennisclubmanager.tennisclub.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // NOTA: Este método es legacy. En el nuevo sistema, los pagos se crean automáticamente
        // cuando se crea un contrato. Este método se mantiene por compatibilidad.
        
        // Buscar usuario por email si es posible, sino usar licenseNumber (legacy)
        // Por ahora, crear pago sin validar player ya que el nuevo sistema usa userEmail
        PaymentEntity payment = paymentMapper.toEntity(request);
        payment.setStatus(PaymentStatus.PENDING);

        // Validar y calcular montos según el tipo
        validateAndSetPaymentDetails(payment, request);

        PaymentEntity savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(UUID paymentId, PaymentRequest request) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Pago no encontrado"));

        // NOTA: En el nuevo sistema, los pagos están asociados a contratos
        // Este método se mantiene por compatibilidad con el sistema legacy

        payment.setAmount(request.amount());
        payment.setPaymentDate(request.paymentDate());
        payment.setNotes(request.notes());

        validateAndSetPaymentDetails(payment, request);

        PaymentEntity updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Pago no encontrado"));

        payment.setStatus(status);
        PaymentEntity updatedPayment = paymentRepository.save(payment);
        
        return paymentMapper.toResponse(updatedPayment);
    }
    

    @Override
    public PaymentResponse getPaymentById(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Pago no encontrado"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByPlayer(String identifier) {
        // El parámetro puede ser un email o un licenseNumber
        // Si contiene '@', es un email; si no, es un licenseNumber
        String userEmail;
        
        if (identifier.contains("@")) {
            // Es un email, usarlo directamente
            userEmail = identifier;
        } else {
            // Es un licenseNumber, buscar el usuario y obtener su email
            userEmail = userRepository.findByLicenseNumber(identifier)
                    .map(UserEntity::getEmail)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Usuario no encontrado con licenseNumber: " + identifier
                    ));
        }
        
        // Buscar todos los pagos por userEmail
        List<PaymentEntity> payments = paymentRepository.findByUserEmailOrderByPaymentDateDesc(userEmail);
        
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPendingPaymentsByPlayer(String identifier) {
        // El parámetro puede ser un email o un licenseNumber
        // Si contiene '@', es un email; si no, es un licenseNumber
        String userEmail;
        
        if (identifier.contains("@")) {
            // Es un email, usarlo directamente
            userEmail = identifier;
        } else {
            // Es un licenseNumber, buscar el usuario y obtener su email
            userEmail = userRepository.findByLicenseNumber(identifier)
                    .map(UserEntity::getEmail)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Usuario no encontrado con licenseNumber: " + identifier
                    ));
        }
        
        // Buscar pagos pendientes por userEmail
        List<PaymentEntity> pendingPayments = paymentRepository.findByUserEmailAndStatus(
                userEmail, 
                PaymentStatus.PENDING
        );
        
        return pendingPayments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentSummaryResponse getPaymentSummary() {
        List<PaymentEntity> allPayments = paymentRepository.findAll();
        
        BigDecimal totalPaid = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        List<PaymentByTypeSummary> byType = calculateByTypeSummary(allPayments);

        return new PaymentSummaryResponse(totalPaid, totalPending, (int) pendingCount, byType);
    }

    @Override
    public PaymentSummaryResponse getPaymentSummaryByPlayer(String identifier) {
        // El parámetro puede ser un email o un licenseNumber
        // Si contiene '@', es un email; si no, es un licenseNumber
        String userEmail;
        
        if (identifier.contains("@")) {
            // Es un email, usarlo directamente
            userEmail = identifier;
        } else {
            // Es un licenseNumber, buscar el usuario y obtener su email
            userEmail = userRepository.findByLicenseNumber(identifier)
                    .map(UserEntity::getEmail)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Usuario no encontrado con licenseNumber: " + identifier
                    ));
        }
        
        // Obtener totales desde el repositorio
        BigDecimal totalPaid = paymentRepository.getTotalPaidByUser(userEmail);
        BigDecimal totalPending = paymentRepository.getTotalPendingByUser(userEmail);
        
        // Contar pagos pendientes
        List<PaymentEntity> pendingPayments = paymentRepository.findByUserEmailAndStatus(
                userEmail, 
                PaymentStatus.PENDING
        );
        int pendingCount = pendingPayments.size();
        
        // Agrupar por tipo (por ahora lista vacía, ya que PaymentType ya no existe directamente)
        List<PaymentByTypeSummary> byType = new ArrayList<>();
        
        return new PaymentSummaryResponse(
                totalPaid != null ? totalPaid : BigDecimal.ZERO,
                totalPending != null ? totalPending : BigDecimal.ZERO,
                pendingCount,
                byType
        );
    }

    @Override
    public List<PaymentResponse> getPaymentsByType(PaymentType paymentType) {
        // En el nuevo sistema, los pagos no tienen PaymentType directo
        // Retornamos lista vacía ya que este método ya no tiene sentido
        // TODO: En el futuro se podría filtrar por tipo de servicio del contrato si es necesario
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void deletePayment(UUID paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado");
        }
        paymentRepository.deleteById(paymentId);
    }

    private void validateAndSetPaymentDetails(PaymentEntity payment, PaymentRequest request) {
        switch (request.paymentType()) {
            case INDIVIDUAL_CLASS:
                // Clase individual: 30 euros
                if (request.amount().compareTo(new BigDecimal("30")) != 0) {
                    payment.setAmount(new BigDecimal("30")); // Forzar el precio correcto
                }
                if (request.classDate() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "La fecha de clase es obligatoria para clases individuales");
                }
                break;

            case CLASS_PACKAGE:
                // Bono de 10 clases: 280 euros
                if (request.amount().compareTo(new BigDecimal("280")) != 0) {
                    payment.setAmount(new BigDecimal("280")); // Forzar el precio correcto
                }
                break;

            case QUARTERLY:
                // Trimestre: 140 euros × días por semana
                if (request.daysPerWeek() == null || request.daysPerWeek() < 1 || request.daysPerWeek() > 5) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Los días por semana deben estar entre 1 y 5");
                }
                BigDecimal quarterlyAmount = new BigDecimal("140").multiply(new BigDecimal(request.daysPerWeek()));
                payment.setAmount(quarterlyAmount);
                break;
        }
    }


    private List<PaymentByTypeSummary> calculateByTypeSummary(List<PaymentEntity> payments) {
        // En el nuevo sistema, los pagos están asociados a contratos, no tienen PaymentType directo
        // Retornamos lista vacía ya que la agrupación por tipo ya no tiene sentido
        // TODO: En el futuro se podría agrupar por tipo de servicio del contrato si es necesario
        return new ArrayList<>();
    }
}









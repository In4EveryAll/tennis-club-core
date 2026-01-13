package com.in4everyall.tennisclubmanager.tennisclub.mapper;

import com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.PaymentResponse;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    @Mapping(target = "paymentNumber", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "invoiceDate", ignore = true)
    PaymentEntity toEntity(PaymentRequest request);
    
    @Mapping(target = "playerLicenseNumber", source = "userEmail")
    @Mapping(
            target = "playerName",
            expression = "java(payment.getContract() != null && payment.getContract().getUser() != null ? " +
                        "payment.getContract().getUser().getFirstName() + \" \" + payment.getContract().getUser().getLastName() : null)"
    )
    @Mapping(target = "paymentType", ignore = true)
    @Mapping(target = "classesRemaining", ignore = true)
    @Mapping(target = "quarterStartDate", ignore = true)
    @Mapping(target = "quarterEndDate", ignore = true)
    @Mapping(target = "daysPerWeek", ignore = true)
    @Mapping(target = "classDate", ignore = true)
    @Mapping(target = "subscriptionId", ignore = true)
    PaymentResponse toResponse(PaymentEntity payment);
}


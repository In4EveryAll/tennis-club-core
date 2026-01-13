package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceResponse;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceStudentsResponse;

import java.util.List;
import java.util.UUID;

public interface ServiceService {
    List<ServiceResponse> getAllServices();
    List<ServiceResponse> getActiveServices();
    ServiceResponse getServiceById(UUID id);
    ServiceStudentsResponse getStudentsByServiceAndPeriod(UUID serviceId, UUID periodId);
}








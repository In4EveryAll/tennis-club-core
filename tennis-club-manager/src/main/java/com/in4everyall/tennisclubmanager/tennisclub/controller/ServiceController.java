package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID id) {
        ServiceResponse service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/{serviceId}/periods/{periodId}/students")
    public ResponseEntity<com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceStudentsResponse> getStudentsByServiceAndPeriod(
            @PathVariable UUID serviceId,
            @PathVariable UUID periodId
    ) {
        com.in4everyall.tennisclubmanager.tennisclub.dto.ServiceStudentsResponse response = 
                serviceService.getStudentsByServiceAndPeriod(serviceId, periodId);
        return ResponseEntity.ok(response);
    }
}








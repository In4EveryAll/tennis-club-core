package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(@RequestBody ContractRequest request) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContractById(@PathVariable UUID id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<ContractResponse>> getContractsByUser(@PathVariable String userEmail) {
        List<ContractResponse> contracts = contractService.getContractsByUserEmail(userEmail);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts() {
        List<ContractResponse> contracts = contractService.getAllContracts();
        return ResponseEntity.ok(contracts);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable UUID id,
            @RequestBody(required = false) java.util.Map<String, Object> updates
    ) {
        ContractResponse response = contractService.updateContract(id, updates);
        return ResponseEntity.ok(response);
    }
}



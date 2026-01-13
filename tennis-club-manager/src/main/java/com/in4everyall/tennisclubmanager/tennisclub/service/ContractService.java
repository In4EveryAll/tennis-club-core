package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.ContractResponse;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    ContractResponse createContract(ContractRequest request);
    ContractResponse getContractById(UUID id);
    List<ContractResponse> getContractsByUserEmail(String userEmail);
    List<ContractResponse> getAllContracts();
    List<ContractResponse> getActiveContracts();
    ContractResponse updateContract(UUID id, java.util.Map<String, Object> updates);
}



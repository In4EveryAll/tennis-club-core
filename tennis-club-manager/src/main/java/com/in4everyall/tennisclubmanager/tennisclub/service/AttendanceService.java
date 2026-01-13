package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AttendanceRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.AttendanceResponse;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    AttendanceResponse createOrUpdateAttendance(AttendanceRequest request);
    List<AttendanceResponse> getAttendancesByEventId(UUID eventId);
    List<AttendanceResponse> getAttendancesByUserId(String userId);
}









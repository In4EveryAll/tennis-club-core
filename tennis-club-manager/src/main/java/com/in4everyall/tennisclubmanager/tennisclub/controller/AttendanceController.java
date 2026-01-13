package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.AttendanceRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.AttendanceResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> createOrUpdateAttendance(@RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.createOrUpdateAttendance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesByEvent(@PathVariable UUID eventId) {
        List<AttendanceResponse> attendances = attendanceService.getAttendancesByEventId(eventId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesByUser(@PathVariable String userId) {
        List<AttendanceResponse> attendances = attendanceService.getAttendancesByUserId(userId);
        return ResponseEntity.ok(attendances);
    }
}









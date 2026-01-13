package com.in4everyall.tennisclubmanager.tennisclub.controller;

import com.in4everyall.tennisclubmanager.tennisclub.dto.EventIntentionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.EventIntentionResponse;
import com.in4everyall.tennisclubmanager.tennisclub.service.EventIntentionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/event-intentions")
@RequiredArgsConstructor
public class EventIntentionController {

    private final EventIntentionService eventIntentionService;

    @PostMapping
    public ResponseEntity<EventIntentionResponse> createOrUpdateIntention(@RequestBody EventIntentionRequest request) {
        EventIntentionResponse response = eventIntentionService.createOrUpdateIntention(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventIntentionResponse>> getIntentionsByEvent(@PathVariable UUID eventId) {
        List<EventIntentionResponse> intentions = eventIntentionService.getIntentionsByEventId(eventId);
        return ResponseEntity.ok(intentions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventIntentionResponse>> getIntentionsByUser(@PathVariable String userId) {
        List<EventIntentionResponse> intentions = eventIntentionService.getIntentionsByUserId(userId);
        return ResponseEntity.ok(intentions);
    }
}









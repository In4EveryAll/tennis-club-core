package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.EventIntentionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.EventIntentionResponse;

import java.util.List;
import java.util.UUID;

public interface EventIntentionService {
    EventIntentionResponse createOrUpdateIntention(EventIntentionRequest request);
    List<EventIntentionResponse> getIntentionsByEventId(UUID eventId);
    List<EventIntentionResponse> getIntentionsByUserId(String userId);
}









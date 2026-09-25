package com.project.AIOrchestratorService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TripDetails(
        UUID itineraryId,
        String destination,
        String origin,
        String departureTime,
        String tripType,
        String specialRequests) {
}

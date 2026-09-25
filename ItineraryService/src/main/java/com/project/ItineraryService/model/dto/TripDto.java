package com.project.ItineraryService.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TripDto {
    UUID itineraryId;
    String destination;
    String origin;
    Date departureTime;
    String tripType;
    String specialRequests;
}

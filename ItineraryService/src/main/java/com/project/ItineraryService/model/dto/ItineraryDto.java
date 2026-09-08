package com.project.ItineraryService.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ItineraryDto {
    private UUID userId;
    private List<DayItineraryDto> dayItineraries;
}

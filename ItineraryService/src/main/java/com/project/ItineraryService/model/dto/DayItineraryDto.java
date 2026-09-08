package com.project.ItineraryService.model.dto;

import com.project.ItineraryService.model.Events;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DayItineraryDto {
    private int dayNo;
    private List<Events> events;
}

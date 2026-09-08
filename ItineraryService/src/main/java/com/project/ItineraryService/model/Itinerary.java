package com.project.ItineraryService.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itineraryId;
    private UUID userId;
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL)
    private List<DayItinerary> dayItineraryList;
}

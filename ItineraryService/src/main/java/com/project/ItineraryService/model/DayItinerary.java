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
public class DayItinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID dayItineraryId;
    private int dayNo;
    @OneToMany(mappedBy = "dayItinerary", cascade = CascadeType.ALL)
    private List<Events> eventsList;
    @ManyToOne
    @JoinColumn(name = "itineraryId")
    private Itinerary itinerary;
}

package com.project.ItineraryService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID eventsId;
    private String eventName;
    private String description;
    private Date eventFrom;
    private Date eventTo;
    @ManyToOne
    @JoinColumn(name = "dayItineraryId")
    @JsonIgnore
    private DayItinerary dayItinerary;
}

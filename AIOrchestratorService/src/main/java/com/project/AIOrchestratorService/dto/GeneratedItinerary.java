package com.project.AIOrchestratorService.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record GeneratedItinerary(
        @JsonPropertyDescription("One entry per day of the trip, ordered by dayNo starting at 1.")
        List<GeneratedDay> dayItineraryList) {

    public record GeneratedDay(
            @JsonPropertyDescription("Day number within the trip, starting at 1.")
            int dayNo,

            @JsonPropertyDescription("Three to six activities for this day, ordered by start time.")
            List<GeneratedEvent> eventsList) {
    }

    public record GeneratedEvent(
            @JsonPropertyDescription("Short title of the activity, for example 'Shikara ride on Dal Lake'.")
            String eventName,

            @JsonPropertyDescription("Two or three sentences describing the activity and why it is worth doing.")
            String description,

            @JsonPropertyDescription("Start date-time in ISO-8601 format yyyy-MM-dd'T'HH:mm:ss, for example "
                    + "2026-04-12T09:30:00. Must be a real date-time on this day's calendar date. "
                    + "Placeholders such as TBD, N/A, unknown or an empty string are forbidden.")
            String eventFrom,

            @JsonPropertyDescription("End date-time in ISO-8601 format yyyy-MM-dd'T'HH:mm:ss, strictly after "
                    + "eventFrom and on the same calendar date. "
                    + "Placeholders such as TBD, N/A, unknown or an empty string are forbidden.")
            String eventTo) {
    }
}

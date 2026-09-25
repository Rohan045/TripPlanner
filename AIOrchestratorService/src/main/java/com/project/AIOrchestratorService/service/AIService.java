package com.project.AIOrchestratorService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AIOrchestratorService.dto.GeneratedItinerary;
import com.project.AIOrchestratorService.dto.TripDetails;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class AIService {

    private static final int DEFAULT_TRIP_DAYS = 3;

    private static final String USER_PROMPT = """
            Plan a {days}-day trip and return it as JSON.

            Destination: {destination}
            Travelling from: {origin}
            Day 1 date: {startDate}
            Last day date: {endDate}
            Trip type: {tripType}
            Special requests: {specialRequests}

            Requirements:
            - Return exactly {days} entries in dayItineraryList, with dayNo running from 1 to {days}.
            - Day 1 is dated {startDate} and every following day is the next calendar date,
              so the last day is dated {endDate}.
            - Every eventFrom and eventTo must use that day's date with a real clock time,
              written as yyyy-MM-dd'T'HH:mm:ss. Do not write TBD, N/A or unknown anywhere.
            - Day 1 must begin with the journey from {origin} to {destination}, and the last
              day must end with the return journey to {origin}.
            - Give each day 3 to 6 events covering sightseeing, meals and rest.
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AIService(ChatClient itineraryChatClient, ObjectMapper objectMapper) {
        this.chatClient = itineraryChatClient;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "trip-details", groupId = "tp-group")
    public void consume(String message) throws Exception {
        System.out.println("Received: " + message);

        TripDetails trip = objectMapper.readValue(message, TripDetails.class);
        LocalDate startDate = parseStartDate(trip.departureTime());
        LocalDate endDate = startDate.plusDays(DEFAULT_TRIP_DAYS - 1L);

        GeneratedItinerary itinerary = chatClient.prompt()
                .user(u -> u.text(USER_PROMPT)
                        .param("days", DEFAULT_TRIP_DAYS)
                        .param("destination", blankToDefault(trip.destination(), "an interesting destination"))
                        .param("origin", blankToDefault(trip.origin(), "the traveller's home city"))
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("tripType", blankToDefault(trip.tripType(), "leisure"))
                        .param("specialRequests", blankToDefault(trip.specialRequests(), "none")))
                .call()
                .entity(GeneratedItinerary.class);

        System.out.println("Itinerary id: " + trip.itineraryId());
        for (GeneratedItinerary.GeneratedDay day : itinerary.dayItineraryList()) {
            System.out.println("Day " + day.dayNo());
            for (GeneratedItinerary.GeneratedEvent event : day.eventsList()) {
                System.out.println("  " + event.eventFrom() + " -> " + event.eventTo()
                        + " : " + event.eventName());
                System.out.println("    " + event.description());
            }
        }
    }

    /**
     * The producer may serialise departureTime as epoch millis or as an ISO-8601 string,
     * so every supported shape is tried before falling back to today.
     */
    private static LocalDate parseStartDate(String rawDepartureTime) {
        if (rawDepartureTime == null || rawDepartureTime.isBlank()) {
            return LocalDate.now();
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(rawDepartureTime.trim()))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (NumberFormatException ignored) {
            // not epoch millis, fall through to the textual formats
        }
        try {
            return OffsetDateTime.parse(rawDepartureTime).toLocalDate();
        } catch (Exception ignored) {
            // not an offset date-time, fall through
        }
        try {
            return LocalDateTime.parse(rawDepartureTime).toLocalDate();
        } catch (Exception ignored) {
            // not a local date-time, fall through
        }
        try {
            return LocalDate.parse(rawDepartureTime);
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }

    private static String blankToDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

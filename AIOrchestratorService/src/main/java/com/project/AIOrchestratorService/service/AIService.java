package com.project.AIOrchestratorService.service;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AIOrchestratorService.dto.GeneratedItinerary;
import com.project.AIOrchestratorService.dto.TripDetails;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class AIService {

    private static final int DEFAULT_TRIP_DAYS = 3;
    private final boolean test = true;

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
    private static final String TOPIC = "itinerary-details";
    private record ItineraryWithId(
            UUID itineraryId,
            @JsonUnwrapped
            GeneratedItinerary itinerary) {}
    @Autowired
    KafkaTemplate<String, ItineraryWithId> kafkaTemplate;

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

        GeneratedItinerary itinerary = test ? createDummyItinerary() : chatClient.prompt()
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
        ItineraryWithId itineraryWithId = new ItineraryWithId(trip.itineraryId(), itinerary);
        sendGeneratedItinerary(itineraryWithId);
    }
    private void sendGeneratedItinerary(ItineraryWithId generatedItinerary) throws Exception {
        try {
            kafkaTemplate.send(TOPIC, generatedItinerary).get();
        } catch (Exception exception) {
            throw new Exception("Error while sending trip details upstream");
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
    public static GeneratedItinerary createDummyItinerary() {
        return new GeneratedItinerary(
                List.of(
                        // Day 1
                        new GeneratedItinerary.GeneratedDay(
                                1,
                                List.of(
                                        new GeneratedItinerary.GeneratedEvent(
                                                "Hotel Check-in & Orientation Walk",
                                                "Arrive at the hotel, check into your room, and unpack. Take a short walk nearby to get familiar with the neighborhood.",
                                                "2026-10-10T09:00:00",
                                                "2026-10-10T11:00:00"
                                        ),
                                        new GeneratedItinerary.GeneratedEvent(
                                                "Historic City Center Tour",
                                                "Explore iconic monuments and heritage landmarks accompanied by a local guide. Learn about local architecture and traditions.",
                                                "2026-10-10T11:30:00",
                                                "2026-10-10T14:00:00"
                                        ),
                                        new GeneratedItinerary.GeneratedEvent(
                                                "Sunset Waterfront Cruise",
                                                "Enjoy scenic views along the riverfront during golden hour. Relax with evening refreshments on board.",
                                                "2026-10-10T17:00:00",
                                                "2026-10-10T19:30:00"
                                        )
                                )
                        ),
                        // Day 2
                        new GeneratedItinerary.GeneratedDay(
                                2,
                                List.of(
                                        new GeneratedItinerary.GeneratedEvent(
                                                "Local Street Food & Market Tour",
                                                "Visit bustling local markets and sample authentic street cuisine. Interact with local vendors and taste artisanal dishes.",
                                                "2026-10-11T09:30:00",
                                                "2026-10-11T12:00:00"
                                        ),
                                        new GeneratedItinerary.GeneratedEvent(
                                                "Museum & Fine Arts Gallery Visit",
                                                "Discover regional historical artifacts and modern art collections inside the city's premier museum.",
                                                "2026-10-11T14:00:00",
                                                "2026-10-11T16:30:00"
                                        ),
                                        new GeneratedItinerary.GeneratedEvent(
                                                "Traditional Fine Dining Experience",
                                                "Indulge in a multi-course dinner featuring traditional regional recipes in a high-rated restaurant.",
                                                "2026-10-11T19:00:00",
                                                "2026-10-11T21:30:00"
                                        )
                                )
                        )
                )
        );
    }
}

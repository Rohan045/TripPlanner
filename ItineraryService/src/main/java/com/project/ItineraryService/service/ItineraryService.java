package com.project.ItineraryService.service;

import com.project.ItineraryService.model.Itinerary;
import com.project.ItineraryService.model.DayItinerary;
import com.project.ItineraryService.model.dto.TripDto;
import com.project.ItineraryService.repository.ItineraryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItineraryService {
    @Autowired
    ItineraryRepo itineraryRepo;

    private static final String TOPIC = "trip-details";

    // KafkaTemplate is auto-configured by Spring Boot
    @Autowired
    private KafkaTemplate<String, TripDto> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void addItinerary(Itinerary itinerary){
        if (itinerary.getDayItineraryList() != null) {
            for (DayItinerary dayItinerary : itinerary.getDayItineraryList()) {
                // DayItinerary owns the itinerary foreign key.
                dayItinerary.setItinerary(itinerary);

                if (dayItinerary.getEventsList() != null) {
                    // Events owns the dayItinerary foreign key.
                    dayItinerary.getEventsList()
                            .forEach(event -> event.setDayItinerary(dayItinerary));
                }
            }
        }
        itineraryRepo.save(itinerary);
    }

    public Itinerary getItinerary(UUID itineraryId){
        Optional<Itinerary> itinerary = itineraryRepo.findById(itineraryId);
        return itinerary.orElse(null);
    }

    public List<Itinerary> getUserItineraries(UUID userId){
        return itineraryRepo.findByUserId(userId);
    }

    public UUID saveEmptyItinerary(UUID userId){
        Itinerary itinerary = new Itinerary();
        itinerary.setUserId(userId);
        itineraryRepo.save(itinerary);
        return itinerary.getItineraryId();
    }

    public void sendTripDetails(TripDto tripDto) throws Exception{
        try {
            kafkaTemplate.send(TOPIC, tripDto).get();
        } catch (Exception exception) {
            throw new Exception("Error while sending trip details upstream");
        }
    }

    @KafkaListener(topics = "itinerary-details", groupId = "tp-group")
    public void consumeItinerary(String message) throws Exception{
        System.out.println("Received message: " + message);
        Itinerary itinerary = objectMapper.readValue(message, Itinerary.class);
        Itinerary savedItinerary = getItinerary(itinerary.getItineraryId());
        if(savedItinerary == null){
            throw new Exception("Itinerary Not Found");
        }
        itinerary.setUserId(savedItinerary.getUserId());
        addItinerary(itinerary);
    }
}

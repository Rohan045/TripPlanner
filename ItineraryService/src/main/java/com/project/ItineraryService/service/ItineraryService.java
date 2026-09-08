package com.project.ItineraryService.service;

import com.project.ItineraryService.model.Itinerary;
import com.project.ItineraryService.repository.ItineraryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ItineraryService {
    @Autowired
    ItineraryRepo itineraryRepo;

    public void addItinerary(Itinerary itinerary){
        itineraryRepo.save(itinerary);
    }

    public Itinerary getItinerary(UUID itineraryId){
        Optional<Itinerary> itinerary = itineraryRepo.findById(itineraryId);
        return itinerary.orElse(null);
    }
}

package com.project.ItineraryService.controller;

import com.project.ItineraryService.model.Itinerary;
import com.project.ItineraryService.model.dto.ItineraryDto;
import com.project.ItineraryService.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    @Autowired
    ItineraryService itineraryService;

    @PostMapping("add")
    public ResponseEntity<Itinerary> addItinerary(@RequestBody Itinerary itinerary){
        itineraryService.addItinerary(itinerary);
        Itinerary savedItinerary = itineraryService.getItinerary(itinerary.getItineraryId());
        if(savedItinerary == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedItinerary);
    }

    @GetMapping("get/{id}")
    public ResponseEntity<Itinerary> getItinerary(@PathVariable("id") UUID id){
        Itinerary itinerary = itineraryService.getItinerary(id);
        if(itinerary == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(itinerary);
    }
}

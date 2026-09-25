package com.project.ItineraryService.controller;

import com.project.ItineraryService.model.Itinerary;
import com.project.ItineraryService.model.dto.TripDto;
import com.project.ItineraryService.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    @Autowired
    ItineraryService itineraryService;

    @PostMapping("add")
    public ResponseEntity<Itinerary> addItinerary(@RequestHeader(value = "X-User-Id", required = true, defaultValue = "defaultVal") String userId, @RequestBody Itinerary itinerary){
        itinerary.setUserId(UUID.fromString(userId));
        itineraryService.addItinerary(itinerary);
        Itinerary savedItinerary = itineraryService.getItinerary(itinerary.getItineraryId());
        if(savedItinerary == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedItinerary);
    }

    @GetMapping("get/{id}")
    public ResponseEntity<Itinerary> getItinerary(@RequestHeader(value = "X-User-Id", required = true, defaultValue = "defaultVal") String userId, @PathVariable("id") UUID id) throws Exception {
        Itinerary itinerary = itineraryService.getItinerary(id);
        if(itinerary == null){
            return ResponseEntity.notFound().build();
        }
        if(!UUID.fromString(userId).equals(itinerary.getUserId())){
            throw new Exception("Itinerary doesn't belong to this user id");
        }
        return ResponseEntity.ok(itinerary);
    }

    @GetMapping("get")
    public ResponseEntity<List<Itinerary>> getUserItineraries(@RequestHeader(value = "X-User-Id", required = true, defaultValue = "defaultVal") String userId){
        List<Itinerary> itineraries = itineraryService.getUserItineraries(UUID.fromString(userId));
        return ResponseEntity.ok(itineraries);
    }

    //Post mapping to get the input of the trip detail
    @PostMapping("tripDetails")
    public ResponseEntity<Object> userTripInput(@RequestHeader(value = "X-User-Id", required = true, defaultValue = "defaultVal") String userId, @RequestBody TripDto tripDto) throws Exception{
        //First save itinerary in processing state
        UUID itineraryId = itineraryService.saveEmptyItinerary(UUID.fromString(userId));
        //sends trip details to kafka topic with itinerary id
        tripDto.setItineraryId(itineraryId);
        itineraryService.sendTripDetails(tripDto);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("update")
    public ResponseEntity<Itinerary> updateItinerary(@RequestBody Itinerary itinerary){
        itineraryService.addItinerary(itinerary);
        Itinerary updatedItinerary = itineraryService.getItinerary(itinerary.getItineraryId());
        return ResponseEntity.ok(updatedItinerary);
    }
}

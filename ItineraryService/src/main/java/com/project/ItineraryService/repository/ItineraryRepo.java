package com.project.ItineraryService.repository;

import com.project.ItineraryService.model.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItineraryRepo extends JpaRepository<Itinerary, UUID> {

    List<Itinerary> findByUserId(UUID userId);
}

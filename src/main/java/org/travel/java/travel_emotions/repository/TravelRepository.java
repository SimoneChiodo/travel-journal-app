package org.travel.java.travel_emotions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.travel.java.travel_emotions.model.Travel;

public interface TravelRepository extends JpaRepository<Travel, Long> {
  // Additional query methods can be defined here if needed
  
}

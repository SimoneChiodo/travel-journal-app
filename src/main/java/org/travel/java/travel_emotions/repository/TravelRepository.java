package org.travel.java.travel_emotions.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.travel.java.travel_emotions.model.Travel;

public interface TravelRepository extends JpaRepository<Travel, Long> {
  // Additional query methods can be defined here if needed
  
  // Filtered Index
  List<Travel> findDistinctByPlaceContainingIgnoreCaseAndFeelingsContainingIgnoreCaseAndTags_IdIn (
    String place,
    String feelings,
    List<Long> tagIds
  );

  // Filtered Index (without tags selected)
  List<Travel> findByPlaceContainingIgnoreCaseAndFeelingsContainingIgnoreCase(String place, String feelings);

  // Sorting Methods
  List<Travel> findByOrderByCostAsc();
  List<Travel> findByOrderByDateAsc();
  List<Travel> findByOrderByDateDesc();

}

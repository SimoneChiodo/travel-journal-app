package org.travel.java.travel_emotions.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.travel.java.travel_emotions.model.Travel;
import org.travel.java.travel_emotions.repository.TravelRepository;

@Service
public class TravelService {
  
  @Autowired
  private TravelRepository travelRepository;

  // INDEX
  public List<Travel> findAll() {
    return travelRepository.findAll();
  }

  // SHOW
  public Travel findById(Long id) {
    return travelRepository.findById(id).get();
  }

  // CREATE
  public Travel save(Travel travel) {
    return travelRepository.save(travel);
  }

  // UPDATE
  public Travel update(Travel travel) {
    return travelRepository.save(travel);
  }

  // DELETE
  public void delete(Long id) {
    if (travelRepository.existsById(id)) 
      travelRepository.deleteById(id);
  }

  // Filtered Index
  public List<Travel> filterTravels(String place, String feelings, List<Long> tagIds) {
    boolean hasPlace = place != null && !place.isBlank();
    boolean hasFeelings = feelings != null && !feelings.isBlank();
    boolean hasTags = tagIds != null && !tagIds.isEmpty();

    // No filters applied
    if (!hasPlace && !hasFeelings && !hasTags) 
      return travelRepository.findAll();
    
    // If tags are selected
    if (hasTags) 
      return travelRepository.findDistinctByPlaceContainingIgnoreCaseAndFeelingsContainingIgnoreCaseAndTags_IdIn(place, feelings, tagIds);
    else // If no tags are selected
      return travelRepository.findByPlaceContainingIgnoreCaseAndFeelingsContainingIgnoreCase(place, feelings);
  }

}

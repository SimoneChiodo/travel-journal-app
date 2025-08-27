package org.travel.java.travel_emotions.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
  public Optional<Travel> findByIdOptional(Long id) {
    return travelRepository.findById(id);
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
  public List<Travel> filterTravels(String place, String date, String cost, String strengthRating, String monetaryRating, List<Long> tagIds) {
    boolean hasPlace = place != null && !place.isBlank();
    boolean hasDate = date != null && !date.isBlank();
    boolean hasCost = cost != null && !cost.isBlank();
    boolean hasStrengthRating = strengthRating != null && !strengthRating.isBlank();
    boolean hasMonetaryRating = monetaryRating != null && !monetaryRating.isBlank();
    boolean hasTags = tagIds != null && !tagIds.isEmpty();

    // Convert String inputs to appropriate types
    LocalDate parsedDate = hasDate ? LocalDate.parse(date) : LocalDate.of(1900, 1, 1); // default "old date" (show all after this date)
    BigDecimal parsedCost = hasCost ? new BigDecimal(cost) : BigDecimal.valueOf(Double.MAX_VALUE); // default infinite (show all)
    Integer parsedStrengthRating = hasStrengthRating ? Integer.valueOf(strengthRating) : 5; // default max rating (show all)
    Integer parsedMonetaryRating = hasMonetaryRating ? Integer.valueOf(monetaryRating) : 5; // default max rating (show all)

    // No filters applied
    if (!hasPlace && !hasDate && !hasCost && !hasStrengthRating && !hasMonetaryRating && !hasTags) 
      return travelRepository.findAll();
    
    // If tags are selected
    if (hasTags) 
      return travelRepository.findDistinctByPlaceContainingIgnoreCaseAndDateGreaterThanEqualAndCostLessThanEqualAndStrengthRatingLessThanEqualAndMonetaryRatingLessThanEqualAndTags_IdIn(place, parsedDate, parsedCost, parsedStrengthRating, parsedMonetaryRating, tagIds);
    else // If no tags are selected
      return travelRepository.findByPlaceContainingIgnoreCaseAndDateGreaterThanEqualAndCostLessThanEqualAndStrengthRatingLessThanEqualAndMonetaryRatingLessThanEqual(place, parsedDate, parsedCost, parsedStrengthRating, parsedMonetaryRating);
  }

}

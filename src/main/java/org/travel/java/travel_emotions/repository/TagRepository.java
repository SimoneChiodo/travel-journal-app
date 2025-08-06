package org.travel.java.travel_emotions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.travel.java.travel_emotions.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
  // Additional query methods can be defined here if needed
  
}

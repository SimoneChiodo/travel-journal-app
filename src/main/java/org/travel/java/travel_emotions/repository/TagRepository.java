package org.travel.java.travel_emotions.repository;

import javax.swing.text.html.HTML.Tag;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
  // Additional query methods can be defined here if needed
  
}

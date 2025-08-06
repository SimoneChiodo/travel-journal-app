package org.travel.java.travel_emotions.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.travel.java.travel_emotions.model.Tag;
import org.travel.java.travel_emotions.repository.TagRepository;

@Service
public class TagService {
  
  @Autowired
  private TagRepository tagRepository;

  // INDEX
  public List<Tag> findAll() {
    return tagRepository.findAll();
  }

  // SHOW
  public Tag findById(Long id) {
    return tagRepository.findById(id).get();
  }

  // CREATE
  public Tag save(Tag tag) {
    return tagRepository.save(tag);
  }

  // UPDATE
  public Tag update(Tag tag) {
    return tagRepository.save(tag);
  }

  // DELETE
  public void delete(Long id) {
    if (tagRepository.existsById(id)) 
      tagRepository.deleteById(id);
  }

}

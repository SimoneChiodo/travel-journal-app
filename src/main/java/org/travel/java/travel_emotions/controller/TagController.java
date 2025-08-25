package org.travel.java.travel_emotions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.travel.java.travel_emotions.model.Tag;
import org.travel.java.travel_emotions.model.Travel;
import org.travel.java.travel_emotions.service.TagService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tag")
public class TagController {
  
  @Autowired
  private TagService tagService;

  // INDEX
  @GetMapping("")
  public String index(Model model) {
    model.addAttribute("tags", tagService.findAll());
    return "tags/index"; 
  }

  // SHOW
  @GetMapping("/{id}")
  public String show(@PathVariable Long id, Model model) {
    Tag tag = tagService.findByIdOptional(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
    model.addAttribute("tag", tag);
    return "tags/show"; 
  }

  // CREATE
  @GetMapping("/create")
  public String create(Model model) {
    model.addAttribute("tag", new Tag());
    model.addAttribute("isCreate", true);
    return "tags/create-or-edit"; 
  }

  // SAVE
  @PostMapping("/create")
  public String save(@Valid @ModelAttribute Tag formTag, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("tag", formTag);
      model.addAttribute("isCreate", true);
      return "tags/create-or-edit";
    }

    // Check if a tag with the same name already exists
    try{
      tagService.save(formTag);
      return "redirect:/tag";
    } catch (Exception e) {
      model.addAttribute("tag", formTag);
      model.addAttribute("isCreate", true);
      bindingResult.rejectValue("name", "error.tag", "A tag with this name already exists.");
      return "tags/create-or-edit";
    }
  }

  // EDIT
  @GetMapping("/edit/{id}")
  public String edit(@PathVariable Long id, Model model) {
    Tag tag = tagService.findById(id);
    model.addAttribute("tag", tag);
    model.addAttribute("isCreate", false);
    return "tags/create-or-edit";
  }

  // UPDATE
  @PostMapping("/edit/{id}")
  public String update(@Valid @ModelAttribute Tag formTag, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("tag", formTag);
      model.addAttribute("isCreate", false);
      return "tags/create-or-edit";
    }

    // Check if a tag with the same name already exists
    try{
      tagService.update(formTag);
      return "redirect:/tag";
    } catch (Exception e) {
      model.addAttribute("tag", formTag);
      model.addAttribute("isCreate", false);
      bindingResult.rejectValue("name", "error.tag", "A tag with this name already exists.");
      return "tags/create-or-edit";
    }
  }

  // DELETE
  @GetMapping("/delete/{id}")
  public String delete(@PathVariable Long id) {
    // Remove tag from all Travel
    Tag tag = tagService.findById(id);
    for (Travel travel : tag.getTravels()) 
      travel.getTags().remove(tag);

    tagService.delete(id);
    return "redirect:/tag";
  }
  
}

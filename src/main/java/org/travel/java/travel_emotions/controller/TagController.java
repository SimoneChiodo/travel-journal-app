package org.travel.java.travel_emotions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.travel.java.travel_emotions.model.Tag;
import org.travel.java.travel_emotions.service.TagService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tags")
public class TagController {
  
  @Autowired
  private TagService tagService;

  // INDEX
  @GetMapping("/tag")
  public String index(Model model) {
    model.addAttribute("tags", tagService.findAll());
    return "tags/index"; 
  }

  // SHOW
  @GetMapping("/{id}")
  public String show(@PathVariable Long id, Model model) {
    model.addAttribute("tag", tagService.findById(id));
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

    tagService.save(formTag);
    return "redirect:/home";
  }

  // EDIT
  @GetMapping("/edit/{id}")
  public String edit(@PathVariable Long id, Model model) {
    Tag tag = tagService.findById(id);
    model.addAttribute("tag", tag);
    model.addAttribute("isCreate", true);
    return "tags/create-or-edit";
  }

  // UPDATE
  @PostMapping("/edit/{id}")
  public String update(@Valid @ModelAttribute Tag formTag, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("tag", formTag);
      model.addAttribute("isCreate", true);
      return "tags/create-or-edit";
    }

    tagService.update(formTag);
    return "redirect:/tags/" + formTag.getId();
  }

  // DELETE
  @GetMapping("/delete/{id}")
  public String delete(@PathVariable Long id) {
    tagService.delete(id);
    return "redirect:/tags/";
  }
  
}

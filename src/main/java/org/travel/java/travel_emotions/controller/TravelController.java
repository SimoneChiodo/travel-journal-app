package org.travel.java.travel_emotions.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.travel.java.travel_emotions.model.Tag;
import org.travel.java.travel_emotions.model.Travel;
import org.travel.java.travel_emotions.service.TagService;
import org.travel.java.travel_emotions.service.TravelService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class TravelController {
  
  @Autowired
  private TravelService travelService;
  @Autowired
  private TagService tagService;

  @GetMapping("/")
  public String goToHome() {
    return "redirect:/home";
  }

  // INDEX
  @GetMapping("/home")
  public String index(Model model) {
    model.addAttribute("travels", travelService.findAll());
    return "travels/index"; 
  }

  // SHOW
  @GetMapping("/travel/{id}")
  public String show(@PathVariable Long id, Model model) {
    model.addAttribute("travel", travelService.findById(id));
    return "travels/show"; 
  }

  // CREATE
  @GetMapping("/travel/create")
  public String create(Model model) {
    model.addAttribute("travel", new Travel());
    model.addAttribute("tags", tagService.findAll());
    model.addAttribute("isCreate", true);

    return "travels/create-or-edit"; 
  }

  // SAVE
  @PostMapping("/travel/create")
  public String save(@Valid @ModelAttribute Travel formTravel, @RequestParam(required = false) List<Long> selectedTags, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("travel", formTravel);
      model.addAttribute("isCreate", true);
      return "travels/create-or-edit";
    }

    // Se sono stati selezionati tag, li recupero dal DB
    if (selectedTags != null) {
        List<Tag> tags = selectedTags.stream()
          .map(tagService::findById)
          .collect(Collectors.toList());
        formTravel.setTags(tags);
    }
    
    travelService.save(formTravel);
    return "redirect:/home";
  }

  // EDIT
  @GetMapping("/travel/edit/{id}")
  public String edit(@PathVariable Long id, Model model) {
    Travel travel = travelService.findById(id);
    model.addAttribute("travel", travel);
    model.addAttribute("tags", tagService.findAll());
    model.addAttribute("isCreate", false);

    return "travels/create-or-edit";
  }

  // UPDATE
  @PostMapping("/travel/edit/{id}")
  public String update(@Valid @ModelAttribute Travel formTravel, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("travel", formTravel);
      model.addAttribute("isCreate", false);
      return "travels/create-or-edit";
    }

    travelService.update(formTravel);
    return "redirect:/travel/" + formTravel.getId();
  }

  // DELETE
  @GetMapping("/travel/delete/{id}")
  public String delete(@PathVariable Long id) {
    travelService.delete(id);
    return "redirect:/home";
  }

}

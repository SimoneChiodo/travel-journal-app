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
import org.travel.java.travel_emotions.model.Travel;
import org.travel.java.travel_emotions.service.TravelService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/travels")
public class TravelController {
  
  @Autowired
  private TravelService travelService;

  // INDEX
  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("travels", travelService.findAll());
    return "travels/index"; 
  }

  // SHOW
  @GetMapping("/{id}")
  public String show(@PathVariable Long id, Model model) {
    model.addAttribute("travel", travelService.findById(id));
    return "travels/show"; 
  }

  // CREATE
  @GetMapping("/create")
  public String create(Model model) {
    model.addAttribute("travel", new Travel());
    return "travels/create-or-update"; 
  }

  // SAVE
  @GetMapping("/save")
  public String save(@Valid @ModelAttribute Travel formTravel, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("travel", formTravel);
      return "travels/create-or-edit";
    }

    travelService.save(formTravel);
    return "redirect:/travels";
  }

  // EDIT
  @GetMapping("/edit/{id}")
  public String edit(@PathVariable Long id, Model model) {
    Travel travel = travelService.findById(id);
    model.addAttribute("travel", travel);
    return "travels/create-or-edit";
  }

  // UPDATE
  @PostMapping("/edit/{id}")
  public String update(@Valid @ModelAttribute Travel formTravel, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("travel", formTravel);
      return "travels/create-or-edit";
    }

    travelService.update(formTravel);
    return "redirect:/travels/" + formTravel.getId();
  }

  // DELETE
  @GetMapping("/delete/{id}")
  public String delete(@PathVariable Long id) {
    travelService.delete(id);
    return "redirect:/travels/";
  }

}

package org.travel.java.travel_emotions.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    model.addAttribute("search_place", new String());
    model.addAttribute("search_feelings", new String());
    model.addAttribute("search_tags", new ArrayList<Long>());
    model.addAttribute("tags", tagService.findAll());
    model.addAttribute("orderBy", ""); 
    model.addAttribute("sortBy", ""); 

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

  // Dashboard
  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    List<Travel> travels = travelService.findAll();

    // Total cost of travels
    BigDecimal totalCost = travels.stream()
      .map(Travel::getCost)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calcolo data inizio e fine
    Optional<LocalDate> startDate = travels.stream()
      .map(Travel::getDate)
      .min(LocalDate::compareTo);
    Optional<LocalDate> endDate = travels.stream()
      .map(Travel::getDate)
      .max(LocalDate::compareTo);

    model.addAttribute("travels", travels);
    model.addAttribute("totalCost", totalCost);
    model.addAttribute("startDate", startDate.orElse(null));
    model.addAttribute("endDate", endDate.orElse(null));

    return "travels/dashboard"; 
  }

  // Filtered Index
  @PostMapping("/home")
  public String filterTravels(@RequestParam(required=false) String search_place, @RequestParam(required=false) String search_feelings, @RequestParam(required=false) List<Long> search_tags, @RequestParam(required=false) String orderBy, @RequestParam(required = false) String sortBy, Model model) {
    // Sorting Methods
    orderBy = orderBy != null ? orderBy : "";
    model.addAttribute("orderBy", orderBy); 
    sortBy = sortBy != null ? sortBy : "asc";
    model.addAttribute("sortBy", sortBy); 
    // Filtered travels
    List<Travel> travels = travelService.filterTravels(search_place, search_feelings, search_tags);
    travels = sortTravels(travels, orderBy, sortBy);
    model.addAttribute("travels", travels); 
    // All tags
    model.addAttribute("tags", tagService.findAll()); 
    // Filters
    model.addAttribute("search_place", search_place != null ? search_place : "");
    model.addAttribute("search_feelings", search_feelings != null ? search_feelings : "");
    model.addAttribute("search_tags", search_tags != null ? search_tags : new ArrayList<Long>());
    
    return "travels/index";
  }

  private List<Travel> sortTravels(List<Travel> travels, String orderBy, String sortBy) {
    // If orderBy is not default
    if (orderBy != "") {
      // Ascending order
      if(sortBy.equals("asc")){
        switch (orderBy) {
          case "cost":
            travels.sort(Comparator.comparing(Travel::getCost));
            break;
          case "date":
            travels.sort(Comparator.comparing(Travel::getDate));
            break;
        }
      } else { // Descending order
        switch (orderBy) {
          case "cost":
            travels.sort(Comparator.comparing(Travel::getCost).reversed());
            break;
          case "date":
            travels.sort(Comparator.comparing(Travel::getDate).reversed());
            break;
        }
      }
    } else { // If orderBy is default
      if(sortBy.equals("desc"))
        Collections.reverse(travels);
    }

    // Don't sort
    return travels;
  }

}

package org.travel.java.travel_emotions.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
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

  // Costants for local paths
  private static final String PROJECT_DIR = System.getProperty("user.dir");
  private static final String PHOTO_DIR = PROJECT_DIR + "/uploads/images";
  private static final String VIDEO_DIR = PROJECT_DIR + "/uploads/videos";

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
    Travel travel = travelService.findByIdOptional(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel not found"));
    model.addAttribute("travel", travel);
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
  public String addTravel(
    @Valid @ModelAttribute Travel formTravel,
    BindingResult bindingResult,
    @RequestParam(required = false) List<Long> selectedTags,
    @RequestParam(value = "photoFiles", required = false) MultipartFile[] photoFiles,
    @RequestParam(value = "videoFiles", required = false) MultipartFile[] videoFiles,
    @RequestParam(required = false) List<String> photoLinks,
    @RequestParam(required = false) List<String> videoLinks,
    Model model
  ) throws IOException {
    // Check errors
    if (bindingResult.hasErrors()) {
        model.addAttribute("travel", formTravel);
        model.addAttribute("isCreate", true);
        model.addAttribute("tags", tagService.findAll());
        return "travels/create-or-edit";
    }

    // Add tags
    if (selectedTags != null) 
      formTravel.setTags(selectedTags.stream().map(actualId -> tagService.findById(actualId)).collect(Collectors.toList()));

    // If there arent photos or videos
    if (formTravel.getPhotos() == null) formTravel.setPhotos(new ArrayList<>());
    if (formTravel.getVideos() == null) formTravel.setVideos(new ArrayList<>());

    // Create Link Photos
    if (photoLinks != null) {
      for (String link : photoLinks) {
        link = link.trim(); // Remove white spaces
        if (!link.isEmpty() && !formTravel.getPhotos().contains(link)) // Avoid duplicates
          formTravel.getPhotos().add(link);
      }
    }

    // Create File Photos
    if (photoFiles != null) {
      // Create a directory if it does not exists
      Files.createDirectories(Paths.get(PHOTO_DIR));
      
      for (MultipartFile file : photoFiles) {
        if (!file.isEmpty()) { // Check if the file is uploaded correctly
          String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // Create an unique file name
          Path filePath = Paths.get(PHOTO_DIR, filename); // Create the file path
          file.transferTo(filePath.toFile()); // Path transformed into a file where the photo data is transferred
          String relativePath = "/uploads/images/" + filename; // Create local path
          if (!formTravel.getPhotos().contains(relativePath)) // Avoid duplicates
            formTravel.getPhotos().add(relativePath);
        }
      }
    }

    // Create Link Videos (hidden input)
    if (videoLinks != null) {
      for (String link : videoLinks) {
        link = link.trim(); // Remove white spaces
        if (!link.isEmpty()) {
          String embedLink = link.startsWith("https://www.youtube.com/embed/") ? link : convertToYouTubeEmbed(link); // Convert Youtube video to embed format
          if (!formTravel.getVideos().contains(embedLink))  // Avoid duplicates
            formTravel.getVideos().add(embedLink);
        }
      }
    }

    // Create File Videos
    if (videoFiles != null) {
      // Create a directory if it does not exists
      Files.createDirectories(Paths.get(VIDEO_DIR));

      for (MultipartFile file : videoFiles) {
        if (!file.isEmpty()) { // Check if the file is uploaded correctly
          String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // Create an unique file name
          Path filePath = Paths.get(VIDEO_DIR, filename); // Create the file path
          file.transferTo(filePath.toFile()); // Path transformed into a file where the video data is transferred
          String relativePath = "/uploads/videos/" + filename; // Create local path
          if (!formTravel.getVideos().contains(relativePath)) // Avoid duplicates
            formTravel.getVideos().add(relativePath);
        }
      }
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
  public String update(
    @PathVariable Long id,
    @Valid @ModelAttribute Travel formTravel,
    BindingResult bindingResult,
    @RequestParam(required = false) List<Long> selectedTags,
    @RequestParam(value = "photoFiles", required = false) MultipartFile[] photoFiles,
    @RequestParam(value = "videoFiles", required = false) MultipartFile[] videoFiles,
    @RequestParam(required = false) List<String> photoLinks,
    @RequestParam(required = false) List<String> videoLinks,
    Model model
  ) throws IOException {
    // Check errors
    if (bindingResult.hasErrors()) {
      model.addAttribute("travel", formTravel);
      model.addAttribute("isCreate", false);
      model.addAttribute("tags", tagService.findAll());
      return "travels/create-or-edit";
    }

    // Save old photos and videos (used to delete unused photos and videos)
    Travel existingTravel = travelService.findById(formTravel.getId());
    List<String> oldPhotos = new ArrayList<>(existingTravel.getPhotos());
    List<String> oldVideos = new ArrayList<>(existingTravel.getVideos());

    // Update Basic Values
    existingTravel.updateBasicValues(formTravel);

    // Update Tags
    if (selectedTags != null)
      existingTravel.setTags(selectedTags.stream().map(actualId -> tagService.findById(actualId)).collect(Collectors.toList()));
    else
      existingTravel.setTags(new ArrayList<>());

    // To update Images and Videos
    List<String> updatedPhotos = new ArrayList<>(existingTravel.getPhotos());
    List<String> updatedVideos = new ArrayList<>(existingTravel.getVideos());
    
    // Remove photos and videos from form
    if (photoLinks != null) // Keep only those submitted by the form
      updatedPhotos.retainAll(photoLinks);
    if (videoLinks != null) // Keep only those submitted by the form
      updatedVideos.retainAll(videoLinks);

    // Update Link Photos
    if (photoLinks != null) {
      for (String link : photoLinks) {
        link = link.trim(); // Remove white spaces
        if (!link.isEmpty() && !updatedPhotos.contains(link)) // Avoid duplicates
          updatedPhotos.add(link); 
      }
    }

    // Update File Photos
    if (photoFiles != null) {
      // Create a directory if it does not exists
      Files.createDirectories(Paths.get(PHOTO_DIR));

      for (MultipartFile file : photoFiles) {
        if (!file.isEmpty()) { // Check if the file is uploaded correctly
          String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // Create an unique file name
          Path filePath = Paths.get(PHOTO_DIR, filename); // Create the file path
          file.transferTo(filePath.toFile()); // Path transformed into a file where the photo data is transferred
          String relativePath = "/uploads/images/" + filename; // Create local path
          if (!updatedPhotos.contains(relativePath)) // Avoid duplicates
            updatedPhotos.add(relativePath);
        }
      }
    }

    // Update Link Videos 
    existingTravel.setVideos(new ArrayList<>()); // reset videos
    if (videoLinks != null) {
      for (String link : videoLinks) {
        link = link.trim(); // Remove white spaces
        if (!link.isEmpty()) {
          String embedLink = link.startsWith("https://www.youtube.com/embed/") ? link : convertToYouTubeEmbed(link); // Convert Youtube link into embed format
          if (!updatedVideos.contains(embedLink)) // Avoid duplicates
            updatedVideos.add(embedLink);
        }
      }
    }

    // Upload File Videos
    if (videoFiles != null) {
      // Create a directory if it does not exists
      Files.createDirectories(Paths.get(VIDEO_DIR));

      for (MultipartFile file : videoFiles) {
        if (!file.isEmpty()) { // Check if the file is uploaded correctly
          String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // Create an unique file name
          Path filePath = Paths.get(VIDEO_DIR, filename); // Create the file path
          file.transferTo(filePath.toFile()); // Path transformed into a file where the video data is transferred
          String relativePath = "/uploads/videos/" + filename; // Create local path
          if (!updatedVideos.contains(relativePath)) // Avoid duplicates
            updatedVideos.add(relativePath);
        }
      }
    }

    // Actually update photos and videos
    existingTravel.setPhotos(updatedPhotos);
    existingTravel.setVideos(updatedVideos);

    travelService.save(existingTravel); // Update Travel

    // Delete unused videos and photos
    cleanupUnusedFiles(oldPhotos, existingTravel.getPhotos());
    cleanupUnusedFiles(oldVideos, existingTravel.getVideos());

    return "redirect:/travel/" + existingTravel.getId(); 
  }



  // DELETE
  @GetMapping("/travel/delete/{id}")
  public String delete(@PathVariable Long id) throws IOException {
    // Get the photos and videos to delete
    Travel travel = travelService.findById(id); 
    List<String> photosToDelete = new ArrayList<>(travel.getPhotos());
    List<String> videosToDelete = new ArrayList<>(travel.getVideos());

    // Delete the travel
    travelService.delete(id);

    // Clean photos and videos
    cleanupUnusedFiles(photosToDelete, Collections.emptyList());
    cleanupUnusedFiles(videosToDelete, Collections.emptyList());

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
  public String filterTravels(@RequestParam(required=false) String search_place, @RequestParam(required=false) String search_date, 
      @RequestParam(required=false) String search_cost, @RequestParam(required=false) String search_strength_rating, 
      @RequestParam(required=false) String search_monetary_rating, @RequestParam(required=false) List<Long> search_tags, 
      @RequestParam(required=false) String orderBy, @RequestParam(required = false) String sortBy, Model model) {
    // Sorting Methods
    orderBy = orderBy != null ? orderBy : "";
    model.addAttribute("orderBy", orderBy); 
    sortBy = sortBy != null ? sortBy : "asc";
    model.addAttribute("sortBy", sortBy); 

    // Filtered travels
    List<Travel> travels = travelService.filterTravels(search_place, search_date, search_cost, search_strength_rating, search_monetary_rating, search_tags);
    travels = sortTravels(travels, orderBy, sortBy);
    model.addAttribute("travels", travels); 
    // All tags
    model.addAttribute("tags", tagService.findAll()); 
    // Filters
    model.addAttribute("search_place", search_place != null ? search_place : "");
    model.addAttribute("search_date", search_date != null ? search_date : "");
    model.addAttribute("search_cost", search_cost != null ? search_cost : "");
    model.addAttribute("search_strength_rating", search_strength_rating != null ? search_strength_rating : "");
    model.addAttribute("search_monetary_rating", search_monetary_rating != null ? search_monetary_rating : "");
    model.addAttribute("search_tags", search_tags != null ? search_tags : new ArrayList<Long>());
    
    return "travels/index";
  }

  // Function to sort travels
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
          case "strengthRating":
            travels.sort(Comparator.comparing(Travel::getStrengthRating));
            break;
          case "monetaryRating":
            travels.sort(Comparator.comparing(Travel::getMonetaryRating));
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
          case "strengthRating":
            travels.sort(Comparator.comparing(Travel::getStrengthRating).reversed());
            break;
          case "monetaryRating":
            travels.sort(Comparator.comparing(Travel::getMonetaryRating).reversed());
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

  // Function to convert Youtube URL to correct format
  private String convertToYouTubeEmbed(String url) {
    // Create Youtube videos pattern
    String[] patterns = {"https?://(?:www\\.)?youtube\\.com/watch\\?v=([\\w-]+)", // URL Standard  (NOTE: "([\w-]+)" -> Capture video ID)
      "https?://youtu\\.be/([\\w-]+)"}; // URL Short

    for (String pattern : patterns) {
      // Check if the url match the pattern
      Matcher matcher = Pattern.compile(pattern).matcher(url);

      if (matcher.find()) // If there is a match
        return "https://www.youtube.com/embed/" + matcher.group(1); // Create embed URL  (NOTE: "matcher.group(1)" -> the first captured element, in this case its the ID)
    }

    // If its not YouTube, return original URL
    return url;
  }

  // Function to delete unused files
  private void cleanupUnusedFiles(List<String> oldFiles, List<String> newFiles) throws IOException {
    for (String filePath : oldFiles) {
      // If its no longer in the new list and isnt an external URL
      if (!newFiles.contains(filePath) && filePath.startsWith("/uploads/")) {
        // Delete the unused file
        Path path = Paths.get(PROJECT_DIR + filePath);
        Files.deleteIfExists(path);
      }
    }
  }
}

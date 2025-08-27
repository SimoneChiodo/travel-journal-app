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

  // Constants for local paths
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
    // Travel list
    model.addAttribute("travels", travelService.findAll());
    // Filters
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
    // Send 404 error if the travel is not found
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
    @RequestParam(required = false) List<String> photoLinks,
    @RequestParam(required = false) List<String> videoLinks,
    @RequestParam(required = false) MultipartFile[] photoFiles,
    @RequestParam(required = false) MultipartFile[] videoFiles,
    Model model
  ) throws IOException {
    // Check errors
    if (bindingResult.hasErrors()) {
        model.addAttribute("travel", formTravel);
        model.addAttribute("isCreate", true);
        model.addAttribute("tags", tagService.findAll());
        return "travels/create-or-edit";
    }

    // Initialize empty lists for photos and videos if null
    ensureListsInitialized(formTravel);


    // Apply tags selected by user
    applyTags(formTravel, selectedTags);


    // Add photo links from form
    addLinks(formTravel.getPhotos(), photoLinks, false);


    // Add uploaded photo files
    addUploadedFiles(formTravel.getPhotos(), photoFiles, PHOTO_DIR, "/uploads/images/");


    // Add video links from form (convert to YouTube embed if needed)
    addLinks(formTravel.getVideos(), videoLinks, true);


    // Add uploaded video files
    addUploadedFiles(formTravel.getVideos(), videoFiles, VIDEO_DIR, "/uploads/videos/");

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
    @RequestParam(required = false) List<String> photoLinks,
    @RequestParam(required = false) List<String> videoLinks,
    @RequestParam(required = false) MultipartFile[] photoFiles,
    @RequestParam(required = false) MultipartFile[] videoFiles,
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

    // Apply selected tags
    applyTags(existingTravel, selectedTags);


    // Prepare updated photo and video lists
    List<String> updatedPhotos = new ArrayList<>(existingTravel.getPhotos());
    List<String> updatedVideos = new ArrayList<>(existingTravel.getVideos());


    // Retain only links submitted by the form
    if (photoLinks != null) 
      updatedPhotos.retainAll(photoLinks);
    if (videoLinks != null) 
      updatedVideos.retainAll(videoLinks);


    // Add photo links and files
    addLinks(updatedPhotos, photoLinks, false);
    addUploadedFiles(updatedPhotos, photoFiles, PHOTO_DIR, "/uploads/images/");


    // Add video links and files
    addLinks(updatedVideos, videoLinks, true);
    addUploadedFiles(updatedVideos, videoFiles, VIDEO_DIR, "/uploads/videos/");

    // Actually update photos and videos
    existingTravel.setPhotos(updatedPhotos);
    existingTravel.setVideos(updatedVideos);

    // Update Travel
    travelService.save(existingTravel); 

    // Delete unused videos and photos
    cleanupUnusedFiles(oldPhotos, existingTravel.getPhotos());
    cleanupUnusedFiles(oldVideos, existingTravel.getVideos());

    return "redirect:/travel/" + existingTravel.getId(); 
  }

  // DELETE
  @GetMapping("/travel/delete/{id}")
  public String delete(@PathVariable Long id) throws IOException {
    // Get the photos and videos of the travel to delete
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
      .map(travel -> travel.getCost())
      .reduce(BigDecimal.ZERO, (sum, n) -> sum.add(n)); // If there are no travels the value is 0

    // Calcolo data inizio e fine
    Optional<LocalDate> startDate = travels.stream()
      .map(travel -> travel.getDate())
      .min((date1, date2) -> date1.compareTo(date2)); // Take the min date
    Optional<LocalDate> endDate = travels.stream()
      .map(travel -> travel.getDate())
      .max((date1, date2) -> date1.compareTo(date2)); // Take the max date

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
    List<Travel> travels = travelService.filterTravels(search_place, search_date, search_cost, search_strength_rating, search_monetary_rating, search_tags); // Filter result
    travels = sortTravels(travels, orderBy, sortBy); // Sort result
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
            travels.sort(Comparator.comparing(travel -> travel.getCost()));
            break;
          case "date":
            travels.sort(Comparator.comparing(travel -> travel.getDate()));
            break;
          case "strengthRating":
            travels.sort(Comparator.comparing(travel -> travel.getStrengthRating()));
            break;
          case "monetaryRating":
            travels.sort(Comparator.comparing(travel -> travel.getMonetaryRating()));
            break;
        }
      } else { // Descending order
        switch (orderBy) {
          case "cost":
            travels.sort(Comparator.comparing((Travel travel) -> travel.getCost()).reversed());
            break;
          case "date":
            travels.sort(Comparator.comparing((Travel travel) -> travel.getDate()).reversed());
            break;
          case "strengthRating":
            travels.sort(Comparator.comparing((Travel travel) -> travel.getStrengthRating()).reversed());
            break;
          case "monetaryRating":
            travels.sort(Comparator.comparing((Travel travel) -> travel.getMonetaryRating()).reversed());
            break;
        }
      }
    } else // If orderBy is default
      if(sortBy.equals("desc"))
        Collections.reverse(travels);

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
      if (!newFiles.contains(filePath) && filePath.startsWith("/uploads/")) { // If its no longer in the new list and isnt an external URL
        Path path = Paths.get(PROJECT_DIR + filePath); // Get the path of the unused file
        Files.deleteIfExists(path); // Delete the unused file
      }
    }
  }

  // ***********************************
  //     METODS FOR SAVE AND UPDATE
  // ***********************************

  // Initialize empty lists for photos and videos
  private void ensureListsInitialized(Travel travel) {
    if (travel.getPhotos() == null) 
      travel.setPhotos(new ArrayList<>());
    if (travel.getVideos() == null) 
      travel.setVideos(new ArrayList<>());
  }


  // Apply selected tags to the travel entity
  private void applyTags(Travel travel, List<Long> selectedTags) {
    if (selectedTags != null) 
      travel.setTags(selectedTags.stream()
      .map(tagService::findById)
      .collect(Collectors.toList()));
    else
      travel.setTags(new ArrayList<>());
  }


  // Add links to photo or video list, optionally converting YouTube links
  private void addLinks(List<String> targetList, List<String> links, boolean isVideo) {
    if (links == null) return;
    
    for (String link : links) {
      link = link.trim();
      if (link.isEmpty()) continue;
      
      if (isVideo) 
        link = link.startsWith("https://www.youtube.com/embed/") ? link : convertToYouTubeEmbed(link);
      
      if (!targetList.contains(link)) 
        targetList.add(link);
    }
  }


  // Add uploaded files to the target list, saving them on disk
  private void addUploadedFiles(List<String> targetList, MultipartFile[] files, String uploadDir, String urlPrefix) throws IOException {
    if (files == null) return;

    Files.createDirectories(Paths.get(uploadDir));
    for (MultipartFile file : files) {
      if (file.isEmpty()) continue;
    
      String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path filePath = Paths.get(uploadDir, filename);
      file.transferTo(filePath.toFile());
      String relativePath = urlPrefix + filename;
      if (!targetList.contains(relativePath)) 
        targetList.add(relativePath);
    }
  }
}

package org.travel.java.travel_emotions.model;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "travels")
public class Travel {
  
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ElementCollection
  @CollectionTable(name = "travel_photos", joinColumns = @JoinColumn(name = "travel_id"))
  @Column(name = "photo_url")
  private List<String> photos;

  @ElementCollection
  @CollectionTable(name = "travel_videos", joinColumns = @JoinColumn(name = "travel_id"))
  @Column(name = "video_url")
  private List<String> videos;

  @NotBlank(message = "Place cannot be blank")
  private String place;

  @NotBlank(message = "Description cannot be blank")
  private String description;

  private String feelings;

  private String reflectionPos;

  private String reflectionNeg;

  @Min(value = 1, message = "Strength rating must be at least 1")
  @Max(value = 5, message = "Strength rating must be at most 5")
  private int strengthRating;

  @Min(value = 1, message = "Monetary rating must be at least 1")
  @Max(value = 5, message = "Monetary rating must be at most 5")
  private int monetaryRating;

  @NotNull(message = "BigDecimal cannot be null")
  @DecimalMin(value = "0.0", message = "Cost must be at least 0.0")
  private BigDecimal cost;

  @ManyToMany()
  @JoinTable(name = "travel_tags",
            joinColumns = @JoinColumn(name = "travel_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags;

  // Getters and Setters

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<String> getPhotos() {
    return this.photos;
  }

  public void setPhotos(List<String> photos) {
    this.photos = photos;
  }

  public List<String> getVideos() {
    return this.videos;
  }

  public void setVideos(List<String> videos) {
    this.videos = videos;
  }

  public String getPlace() {
    return this.place;
  }

  public void setPlace(String place) {
    this.place = place;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFeelings() {
    return this.feelings;
  }

  public void setFeelings(String feelings) {
    this.feelings = feelings;
  }

  public String getReflectionPos() {
    return this.reflectionPos;
  }

  public void setReflectionPos(String reflectionPos) {
    this.reflectionPos = reflectionPos;
  }

  public String getReflectionNeg() {
    return this.reflectionNeg;
  }

  public void setReflectionNeg(String reflectionNeg) {
    this.reflectionNeg = reflectionNeg;
  }

  public int getStrengthRating() {
    return this.strengthRating;
  }

  public void setStrengthRating(int strengthRating) {
    this.strengthRating = strengthRating;
  }

  public int getMonetaryRating() {
    return this.monetaryRating;
  }

  public void setMonetaryRating(int monetaryRating) {
    this.monetaryRating = monetaryRating;
  }

  public BigDecimal getCost() {
    return this.cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public List<Tag> getTags() {
    return this.tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

}

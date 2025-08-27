package org.travel.java.travel_emotions.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "tags")
public class Tag {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name cannot be blank")
  @Column(unique = true) // This is not handled by Bean Validation, but by the DB -> It's not checked by BindingResult, so I need to check it in the Controller
  private String name;

  @NotBlank(message = "Color cannot be blank")
  @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Color must be a valid hex code")
  private String hexColor;

  @ManyToMany(mappedBy = "tags")
  private List<Travel> travels;

  // Getters and Setters

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHexColor() {
    return this.hexColor;
  }

  public void setHexColor(String hexColor) {
    this.hexColor = hexColor;
  }

  public List<Travel> getTravels() {
    return this.travels;
  }

  public void setTravels(List<Travel> travels) {
    this.travels = travels;
  }

}

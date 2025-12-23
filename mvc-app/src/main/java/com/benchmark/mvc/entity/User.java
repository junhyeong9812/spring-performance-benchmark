package com.benchmark.mvc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String email;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected User() {}

  public User(String username, String email) {
    this.username = username;
    this.email = email;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}

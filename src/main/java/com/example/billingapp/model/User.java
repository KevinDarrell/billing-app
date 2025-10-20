package com.example.billingapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @Column(name = "is_area_head", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    private boolean isAreaHead = false;

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    private boolean enabled = true;

    public User() {}

    public User(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public boolean isAreaHead() { return isAreaHead; }
    public void setAreaHead(boolean areaHead) { isAreaHead = areaHead; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

}
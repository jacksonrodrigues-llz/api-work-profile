package api.work.profile.entity;

import api.work.profile.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import api.work.profile.enums.UserCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;
    
    private String githubUsername;
    private String githubToken;
    private String company;
    private String position;
    private String profilePhoto;
    private String avatar;
    private String avatarUrl;
    private LocalDateTime lastLogin;
    
    private String password;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @Builder.Default
    private Boolean active = true;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = true)
    private UserCategory category;
    
    private String passwordResetToken;
    private LocalDateTime tokenExpiration;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Activity> activities;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Goal> goals;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Achievement> achievements;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TechDebt> techDebts;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
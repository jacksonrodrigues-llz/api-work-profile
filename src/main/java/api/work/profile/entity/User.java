package api.work.profile.entity;

import api.work.profile.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import api.work.profile.enums.UserCategory;

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

    @jakarta.persistence.Column(unique = true, nullable = false)
    private String email;

    @jakarta.persistence.Column(nullable = false)
    private String name;
    
    private String githubUsername;
    private String githubToken;
    private String company;
    private String position;
    private String profilePhoto;
    private String avatar; // Avatar personalizado
    
    // Campos de autenticação
    private String password;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @Builder.Default
    private Boolean active = true;
    
    // Perfil e permissões
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    // Categoria profissional
    @Enumerated(EnumType.STRING)
    @jakarta.persistence.Column(name = "category", nullable = true)
    private UserCategory category;
    
    // Reset de senha
    private String passwordResetToken;
    private LocalDateTime tokenExpiration;

    @jakarta.persistence.Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Activity> activities;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Goal> goals;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Achievement> achievements;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TechDebt> techDebts;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
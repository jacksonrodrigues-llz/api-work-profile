package api.work.profile.entity;

import api.work.profile.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import api.work.profile.enums.UserCategory;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
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
    private String avatar; // Avatar personalizado
    
    // Campos de autenticação
    private String password;
    private Boolean enabled = true;
    
    // Perfil e permissões
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    
    // Categoria profissional
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = true)
    private UserCategory category;
    
    // Reset de senha
    private String passwordResetToken;
    private LocalDateTime tokenExpiration;
    
    @Column(name = "created_at")
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
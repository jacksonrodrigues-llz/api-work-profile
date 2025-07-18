package api.work.profile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private AchievementType type;
    
    private String impact;
    private String recognition;
    
    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (achievedAt == null) {
            achievedAt = LocalDateTime.now();
        }
    }
    
    public enum AchievementType {
        MILESTONE, CERTIFICATION, RECOGNITION, INNOVATION, LEADERSHIP, TECHNICAL
    }
}
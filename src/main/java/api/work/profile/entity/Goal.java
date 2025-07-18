package api.work.profile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "target_date")
    private LocalDate targetDate;
    
    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    private GoalCategory category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = GoalStatus.ACTIVE;
        }
    }
    
    public enum GoalStatus {
        ACTIVE, COMPLETED, PAUSED, CANCELLED
    }
    
    public enum GoalCategory {
        TECHNICAL, LEADERSHIP, PERSONAL, CAREER, PROJECT
    }
}
package api.work.profile.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_columns")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private Integer position;
    
    @Column(name = "icon_class")
    private String iconClass;
    
    @Column(name = "color_class")
    private String colorClass;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;
    
    @Builder.Default
    private Boolean deletable = true;
}
package api.work.profile.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tech_debts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechDebt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String problema;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(nullable = false)
    private Integer prioridade; // 1-Crítico, 2-Alto, 3-Médio, 4-Baixo
    
    @ElementCollection(targetClass = TipoDebito.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tech_debt_tipos", joinColumns = @JoinColumn(name = "tech_debt_id"))
    @Column(name = "tipos")
    private List<TipoDebito> tipos;
    
    @ElementCollection
    @CollectionTable(name = "tech_debt_tags", joinColumns = @JoinColumn(name = "tech_debt_id"))
    @Column(name = "tags")
    private List<String> tags;
    
    @Enumerated(EnumType.STRING)
    private StatusDebito status = StatusDebito.TODO;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;
    
    @Column(name = "criado_por")
    private String criadoPor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (status == StatusDebito.DONE && dataResolucao == null) {
            dataResolucao = LocalDateTime.now();
        }
    }
    
    public enum TipoDebito {
        BACKEND, FRONTEND, UI_UX, INFRA, NEGOCIO
    }
    
    public enum StatusDebito {
        TODO, IN_PROGRESS, PAUSE, CANCELLED, TEST, DEPLOY, DONE
    }
    
    public long getDiasEmAberto() {
        if (dataCriacao == null) return 0;
        LocalDateTime fim = dataResolucao != null ? dataResolucao : LocalDateTime.now();
        return java.time.Duration.between(dataCriacao, fim).toDays();
    }
}
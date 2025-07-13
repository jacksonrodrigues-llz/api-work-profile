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
    
    @Column(nullable = false, length = 500)
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
    
    @Column(name = "criado_por", length = 255)
    private String criadoPor;
    
    @Column(name = "criado_por_id")
    private Long criadoPorId;
    
    @Column(name = "alterado_por_id")
    private Long alteradoPorId;
    
    @Column(name = "data_alteracao")
    private LocalDateTime dataAlteracao;
    
    @Column(name = "task_number", length = 100)
    private String taskNumber;
    
    @Column(name = "task_url", columnDefinition = "TEXT")
    private String taskUrl;
    
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
        dataAlteracao = LocalDateTime.now();
        if (status == StatusDebito.DONE && dataResolucao == null) {
            dataResolucao = LocalDateTime.now();
        }
    }
    
    public enum TipoDebito {
        BACKEND, FRONTEND, UI_UX, INFRA, NEGOCIO;
        
        @com.fasterxml.jackson.annotation.JsonCreator
        public static TipoDebito fromString(String value) {
            if (value == null) return null;
            String normalized = value.toUpperCase().trim();
            
            // Mapeamentos flexíveis
            return switch (normalized) {
                case "BACKEND", "BACK" -> BACKEND;
                case "FRONTEND", "FRONT" -> FRONTEND;
                case "UI_UX", "UI", "UX" -> UI_UX;
                case "INFRA", "INFRAESTRUTURA" -> INFRA;
                case "NEGOCIO", "BUSINESS" -> NEGOCIO;
                default -> valueOf(normalized);
            };
        }
    }
    
    public enum StatusDebito {
        TODO, IN_PROGRESS, PAUSE, CANCELLED, TEST, DEPLOY, DONE;
        
        @com.fasterxml.jackson.annotation.JsonCreator
        public static StatusDebito fromString(String value) {
            if (value == null) return null;
            String normalized = value.toUpperCase().trim().replace("-", "_");
            
            return switch (normalized) {
                case "TODO", "TO_DO", "PENDENTE" -> TODO;
                case "IN_PROGRESS", "INPROGRESS", "PROGRESSO" -> IN_PROGRESS;
                case "PAUSE", "PAUSADO" -> PAUSE;
                case "CANCELLED", "CANCELADO" -> CANCELLED;
                case "TEST", "TESTE" -> TEST;
                case "DEPLOY", "DEPLOYMENT" -> DEPLOY;
                case "DONE", "CONCLUIDO", "FINALIZADO" -> DONE;
                default -> valueOf(normalized);
            };
        }
    }
    
    public long getDiasEmAberto() {
        if (dataCriacao == null) return 0;
        LocalDateTime fim = dataResolucao != null ? dataResolucao : LocalDateTime.now();
        return java.time.Duration.between(dataCriacao, fim).toDays();
    }
}
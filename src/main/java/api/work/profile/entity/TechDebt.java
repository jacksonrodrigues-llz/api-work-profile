package api.work.profile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tech_debts")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TechDebt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(nullable = false, length = 500)
    private String problema;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String descricao;

    @jakarta.persistence.Column(nullable = false)
    private Integer prioridade; // 1-Crítico, 2-Alto, 3-Médio, 4-Baixo
    
    @ElementCollection(targetClass = TipoDebito.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tech_debt_tipos", joinColumns = @JoinColumn(name = "tech_debt_id"))
    @jakarta.persistence.Column(name = "tipos")
    private List<TipoDebito> tipos;
    
    @ElementCollection
    @CollectionTable(name = "tech_debt_tags", joinColumns = @JoinColumn(name = "tech_debt_id"))
    @jakarta.persistence.Column(name = "tags")
    private List<String> tags;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusDebito status = StatusDebito.TODO;

    @jakarta.persistence.Column(name = "criado_por", length = 255)
    private String criadoPor;

    @jakarta.persistence.Column(name = "criado_por_id")
    private Long criadoPorId;

    @jakarta.persistence.Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @jakarta.persistence.Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @jakarta.persistence.Column(name = "alterado_por_id")
    private Long alteradoPorId;

    @jakarta.persistence.Column(name = "data_alteracao")
    private LocalDateTime dataAlteracao;

    @jakarta.persistence.Column(name = "task_number", length = 100)
    private String taskNumber;

    @jakarta.persistence.Column(name = "task_url", columnDefinition = "TEXT")
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
        BACKEND, FRONTEND, UI_UX, INFRA, NEGOCIO, SECURITY, DATABASE;
        
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
                case "SEGURANCA", "LGPD" -> SECURITY;
                case "BANCO", "DB", "BASE DE DADOS", "DATABASE" -> DATABASE;
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
                case "IN_PROGRESS", "INPROGRESS", "PROGRESSO", "PROGRESS" -> IN_PROGRESS;
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
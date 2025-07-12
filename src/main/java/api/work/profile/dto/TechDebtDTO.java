package api.work.profile.dto;

import api.work.profile.entity.TechDebt;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechDebtDTO {
    private String problema;
    private String descricao;
    private Integer prioridade;
    private List<String> tipos;
    private List<String> tags;
    private String status;
    private String dataCriacao;
    private String criadoPor;
    
    public TechDebt toEntity() {
        TechDebt debt = new TechDebt();
        debt.setProblema(this.problema);
        debt.setDescricao(this.descricao);
        debt.setPrioridade(this.prioridade);
        debt.setTags(this.tags);
        debt.setCriadoPor(this.criadoPor);
        
        if (this.tipos != null) {
            debt.setTipos(this.tipos.stream()
                .map(tipo -> {
                    String normalizedTipo = tipo.toUpperCase()
                        .replace("BACKEND", "BACKEND")
                        .replace("FRONTEND", "FRONTEND")
                        .replace("UI_UX", "UI_UX")
                        .replace("UI/UX", "UI_UX")
                        .replace("INFRA", "INFRA")
                        .replace("NEGOCIO", "NEGOCIO")
                        .replace("NEGÓCIO", "NEGOCIO");
                    return TechDebt.TipoDebito.valueOf(normalizedTipo);
                })
                .toList());
        }
        
        if (this.status != null) {
            String normalizedStatus = this.status.toUpperCase()
                .replace("TODO", "TODO")
                .replace("IN_PROGRESS", "IN_PROGRESS")
                .replace("PAUSE", "PAUSE")
                .replace("CANCELLED", "CANCELLED")
                .replace("TEST", "TEST")
                .replace("DEPLOY", "DEPLOY")
                .replace("DONE", "DONE");
            debt.setStatus(TechDebt.StatusDebito.valueOf(normalizedStatus));
        }
        
        if (this.dataCriacao != null) {
            debt.setDataCriacao(LocalDateTime.parse(this.dataCriacao));
        }
        
        return debt;
    }
}
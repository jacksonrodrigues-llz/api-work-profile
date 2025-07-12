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
    private List<TechDebt.TipoDebito> tipos;
    private List<String> tags;
    private TechDebt.StatusDebito status;
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
            debt.setTipos(this.tipos);
        }
        
        if (this.status != null) {
            debt.setStatus(this.status);
        }
        
        if (this.dataCriacao != null) {
            debt.setDataCriacao(LocalDateTime.parse(this.dataCriacao));
        }
        
        return debt;
    }
}
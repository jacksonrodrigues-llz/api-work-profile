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
        var builder = TechDebt.builder()
            .problema(this.problema)
            .descricao(this.descricao)
            .prioridade(this.prioridade)
            .tags(this.tags)
            .criadoPor(this.criadoPor);
        
        if (this.tipos != null) {
            builder.tipos(this.tipos);
        }
        
        if (this.status != null) {
            builder.status(this.status);
        }
        
        if (this.dataCriacao != null) {
            builder.dataCriacao(LocalDateTime.parse(this.dataCriacao));
        }
        
        return builder.build();
    }
}
package api.work.profile.dto;

import lombok.Data;

@Data
public class TechDebtSearchDTO {
    private Long id;
    private String problema;
    private String descricao;
    private String taskNumber;
    
    public TechDebtSearchDTO(Long id, String problema, String descricao, String taskNumber) {
        this.id = id;
        this.problema = problema;
        this.descricao = descricao;
        this.taskNumber = taskNumber;
    }
}
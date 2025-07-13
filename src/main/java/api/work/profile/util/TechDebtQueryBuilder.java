package api.work.profile.util;

import api.work.profile.entity.TechDebt;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TechDebtQueryBuilder {
    
    public static Specification<TechDebt> buildSpecification(
            TechDebt.StatusDebito status,
            Integer prioridade,
            TechDebt.TipoDebito tipo,
            String search,
            String taskNumber,
            LocalDateTime periodo) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            if (prioridade != null) {
                predicates.add(criteriaBuilder.equal(root.get("prioridade"), prioridade));
            }
            
            if (tipo != null) {
                predicates.add(criteriaBuilder.isMember(tipo, root.get("tipos")));
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate problemaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("problema")), searchPattern);
                Predicate descricaoPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("descricao")), searchPattern);
                predicates.add(criteriaBuilder.or(problemaPredicate, descricaoPredicate));
            }
            
            if (taskNumber != null && !taskNumber.trim().isEmpty()) {
                String taskPattern = "%" + taskNumber.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("taskNumber")), taskPattern));
            }
            
            if (periodo != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataCriacao"), periodo));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
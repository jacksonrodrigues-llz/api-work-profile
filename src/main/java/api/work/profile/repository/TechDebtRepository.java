package api.work.profile.repository;

import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TechDebtRepository extends JpaRepository<TechDebt, Long> {
    
    Page<TechDebt> findByUserOrderByDataCriacaoDesc(User user, Pageable pageable);
    
    @Query("SELECT t FROM TechDebt t WHERE t.user = :user " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:prioridade IS NULL OR t.prioridade = :prioridade) " +
           "AND (:tipo IS NULL OR :tipo MEMBER OF t.tipos) " +
           "ORDER BY t.dataCriacao DESC")
    Page<TechDebt> findByUserWithFilters(@Param("user") User user, 
                                        @Param("status") TechDebt.StatusDebito status,
                                        @Param("prioridade") Integer prioridade,
                                        @Param("tipo") TechDebt.TipoDebito tipo,
                                        Pageable pageable);
    
    Long countByUserAndStatus(User user, TechDebt.StatusDebito status);
    
    @Query("SELECT COUNT(t) FROM TechDebt t WHERE t.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT t FROM TechDebt t WHERE t.user = :user AND t.dataCriacao >= :dataInicio")
    List<TechDebt> findByUserAndDataCriacaoAfter(@Param("user") User user, @Param("dataInicio") LocalDateTime dataInicio);
    
    @Query("SELECT COUNT(t) FROM TechDebt t WHERE t.user = :user AND t.prioridade = :prioridade")
    Long countByUserAndPrioridade(@Param("user") User user, @Param("prioridade") Integer prioridade);
}
package api.work.profile.repository;

import api.work.profile.entity.DailyReport;
import api.work.profile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    
    List<DailyReport> findByUserOrderByReportDateDesc(User user);
    
    Optional<DailyReport> findByUserAndReportDate(User user, LocalDate date);
    
    List<DailyReport> findTop2ByUserOrderByReportDateDesc(User user);
}
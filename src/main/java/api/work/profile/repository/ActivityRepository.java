package api.work.profile.repository;

import api.work.profile.entity.Activity;
import api.work.profile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.user = ?1 AND a.status = 'DONE' AND a.completedAt >= ?2")
    Long countCompletedActivitiesSince(User user, LocalDateTime since);
    
    @Query("SELECT AVG(a.actualHours) FROM Activity a WHERE a.user = ?1 AND a.status = 'DONE' AND a.actualHours IS NOT NULL")
    Double getAverageHoursPerActivity(User user);
}
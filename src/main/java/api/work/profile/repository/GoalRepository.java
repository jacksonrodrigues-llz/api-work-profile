package api.work.profile.repository;

import api.work.profile.entity.Goal;
import api.work.profile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = ?1 AND g.status = ?2")
    Long countCompletedGoals(User user, Goal.GoalStatus status);
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = ?1 AND g.status = ?2")
    Long countActiveGoals(User user, Goal.GoalStatus status);
}
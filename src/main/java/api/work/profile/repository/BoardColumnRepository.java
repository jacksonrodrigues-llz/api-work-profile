package api.work.profile.repository;

import api.work.profile.entity.BoardColumn;
import api.work.profile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByUserOrderByPosition(User user);
    boolean existsByUserAndStatus(User user, String status);
}
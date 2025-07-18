package api.work.profile.repository;

import api.work.profile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGithubUsername(String githubUsername);
    Optional<User> findByPasswordResetToken(String token);
}
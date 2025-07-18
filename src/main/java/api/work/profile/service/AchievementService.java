package api.work.profile.service;

import api.work.profile.entity.Achievement;
import api.work.profile.entity.User;
import api.work.profile.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public List<Achievement> getAchievementsByUser(User user) {
        return achievementRepository.findByUserOrderByAchievedAtDesc(user);
    }

    public Achievement saveAchievement(Achievement achievement, User user) {
        var achievementToSave = achievement.toBuilder()
            .user(user)
            .build();
        
        var saved = achievementRepository.save(achievementToSave);
        log.info("[ACHIEVEMENT] Criada: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    public Achievement getAchievementForEdit(Long id, User user) {
        return achievementRepository.findById(id)
            .filter(achievement -> achievement.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException("Conquista não encontrada"));
    }

    public Achievement updateAchievement(Long id, Achievement achievement) {
        var existing = achievementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Conquista não encontrada"));
        
        var updated = existing.toBuilder()
            .title(achievement.getTitle())
            .description(achievement.getDescription())
            .type(achievement.getType())
            .impact(achievement.getImpact())
            .recognition(achievement.getRecognition())
            .achievedAt(achievement.getAchievedAt())
            .build();
        
        var saved = achievementRepository.save(updated);
        log.info("[ACHIEVEMENT] Atualizada: {} (ID: {})", saved.getTitle(), id);
        return saved;
    }
}
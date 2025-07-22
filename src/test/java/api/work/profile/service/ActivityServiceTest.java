package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private Activity todoActivity;
    private Activity inProgressActivity;
    private Activity doneActivity;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Data atual
        now = LocalDateTime.now();

        // Configurar usuário de teste
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        // Configurar atividades de teste
        todoActivity = Activity.builder()
                .id(1L)
                .title("Atividade TODO")
                .description("Descrição da atividade TODO")
                .status(Activity.ActivityStatus.TODO)
                .priority(Activity.Priority.MEDIUM)
                .project("Projeto Teste")
                .skills("Java, Spring")
                .estimatedHours(8)
                .createdAt(now.minusDays(2))
                .user(testUser)
                .build();

        inProgressActivity = Activity.builder()
                .id(2L)
                .title("Atividade IN_PROGRESS")
                .description("Descrição da atividade em progresso")
                .status(Activity.ActivityStatus.IN_PROGRESS)
                .priority(Activity.Priority.HIGH)
                .project("Projeto Teste")
                .skills("Java, Spring")
                .estimatedHours(16)
                .createdAt(now.minusDays(1))
                .user(testUser)
                .build();

        doneActivity = Activity.builder()
                .id(3L)
                .title("Atividade DONE")
                .description("Descrição da atividade concluída")
                .status(Activity.ActivityStatus.DONE)
                .priority(Activity.Priority.LOW)
                .project("Projeto Teste")
                .skills("Java, Spring")
                .estimatedHours(4)
                .actualHours(5)
                .createdAt(now.minusDays(3))
                .completedAt(now.minusHours(2))
                .user(testUser)
                .build();
    }

    @Test
    void getKanbanData_ShouldReturnMapWithActivitiesByStatus() {
        // Arrange
        List<Activity> activities = Arrays.asList(todoActivity, inProgressActivity, doneActivity);
        when(activityRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(activities);

        // Act
        Map<String, Object> result = activityService.getKanbanData(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(activities, result.get("activities"));
        assertEquals(1, ((List<?>) result.get("todoActivities")).size());
        assertEquals(1, ((List<?>) result.get("inProgressActivities")).size());
        assertEquals(0, ((List<?>) result.get("devActivities")).size());
        assertEquals(1, ((List<?>) result.get("doneActivities")).size());
        assertEquals(1, result.get("todoCount"));
        assertEquals(1, result.get("inProgressCount"));
        assertEquals(0, result.get("devCount"));
        assertEquals(1, result.get("doneCount"));
        verify(activityRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    void saveActivity_NewActivity_ShouldSaveAndReturnActivity() {
        // Arrange
        Activity newActivity = Activity.builder()
                .title("Nova Atividade")
                .description("Descrição da nova atividade")
                .status(Activity.ActivityStatus.TODO)
                .priority(Activity.Priority.MEDIUM)
                .project("Projeto Teste")
                .skills("Java, Spring")
                .estimatedHours(8)
                .build();

        when(activityRepository.save(any(Activity.class))).thenReturn(
                newActivity.toBuilder().id(4L).createdAt(now).user(testUser).build()
        );

        // Act
        Activity result = activityService.saveActivity(newActivity, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("Nova Atividade", result.getTitle());
        assertEquals(testUser, result.getUser());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void saveActivity_DoneActivity_ShouldSetCompletedAtAndSave() {
        // Arrange
        Activity doneActivity = Activity.builder()
                .title("Atividade Concluída")
                .description("Descrição da atividade concluída")
                .status(Activity.ActivityStatus.DONE)
                .priority(Activity.Priority.MEDIUM)
                .project("Projeto Teste")
                .skills("Java, Spring")
                .estimatedHours(8)
                .actualHours(7)
                .build();

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            return saved.toBuilder().id(5L).build();
        });

        // Act
        Activity result = activityService.saveActivity(doneActivity, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Atividade Concluída", result.getTitle());
        assertEquals(Activity.ActivityStatus.DONE, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void getActivityForEdit_ExistingActivity_ShouldReturnActivity() {
        // Arrange
        when(activityRepository.findById(1L)).thenReturn(Optional.of(todoActivity));

        // Act
        Activity result = activityService.getActivityForEdit(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Atividade TODO", result.getTitle());
        verify(activityRepository, times(1)).findById(1L);
    }

    @Test
    void getActivityForEdit_NonExistingActivity_ShouldThrowException() {
        // Arrange
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            activityService.getActivityForEdit(99L, testUser);
        });
        verify(activityRepository, times(1)).findById(99L);
    }

    @Test
    void getActivityForEdit_ActivityFromDifferentUser_ShouldThrowException() {
        // Arrange
        User otherUser = User.builder().id(2L).name("Other User").email("other@example.com").build();
        Activity otherUserActivity = todoActivity.toBuilder().user(otherUser).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(otherUserActivity));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            activityService.getActivityForEdit(1L, testUser);
        });
        verify(activityRepository, times(1)).findById(1L);
    }

    @Test
    void updateActivity_ExistingActivity_ShouldUpdateAndReturnActivity() {
        // Arrange
        Activity updatedActivity = Activity.builder()
                .title("Atividade Atualizada")
                .description("Descrição atualizada")
                .status(Activity.ActivityStatus.IN_PROGRESS)
                .priority(Activity.Priority.HIGH)
                .project("Projeto Atualizado")
                .skills("Java, Spring, JPA")
                .estimatedHours(12)
                .actualHours(6)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(todoActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        Activity result = activityService.updateActivity(1L, updatedActivity);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Atividade Atualizada", result.getTitle());
        assertEquals("Descrição atualizada", result.getDescription());
        assertEquals(Activity.ActivityStatus.IN_PROGRESS, result.getStatus());
        assertEquals(Activity.Priority.HIGH, result.getPriority());
        assertEquals("Projeto Atualizado", result.getProject());
        assertEquals("Java, Spring, JPA", result.getSkills());
        assertEquals(12, result.getEstimatedHours());
        assertEquals(6, result.getActualHours());
        verify(activityRepository, times(1)).findById(1L);
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void updateActivity_ToDoneStatus_ShouldSetCompletedAt() {
        // Arrange
        Activity updatedActivity = todoActivity.toBuilder()
                .status(Activity.ActivityStatus.DONE)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(todoActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        Activity result = activityService.updateActivity(1L, updatedActivity);

        // Assert
        assertNotNull(result);
        assertEquals(Activity.ActivityStatus.DONE, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(activityRepository, times(1)).findById(1L);
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void updateActivityStatus_ShouldUpdateStatus() {
        // Arrange
        when(activityRepository.findById(1L)).thenReturn(Optional.of(todoActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        activityService.updateActivityStatus(1L, "IN_PROGRESS");

        // Assert
        verify(activityRepository, times(1)).findById(1L);
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void updateActivityStatus_ToDoneStatus_ShouldSetCompletedAt() {
        // Arrange
        when(activityRepository.findById(1L)).thenReturn(Optional.of(todoActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        activityService.updateActivityStatus(1L, "DONE");

        // Assert
        verify(activityRepository, times(1)).findById(1L);
        verify(activityRepository, times(1)).save(argThat(activity -> 
            activity.getStatus() == Activity.ActivityStatus.DONE && activity.getCompletedAt() != null
        ));
    }
}
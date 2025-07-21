package api.work.profile.service;

import api.work.profile.entity.DailyReport;
import api.work.profile.entity.User;
import api.work.profile.repository.DailyReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    @Mock
    private DailyReportRepository dailyReportRepository;

    @InjectMocks
    private DailyReportService dailyReportService;

    private User testUser;
    private DailyReport testReport;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        // Data atual
        today = LocalDate.now();

        // Configurar relatório diário de teste
        testReport = DailyReport.builder()
                .id(1L)
                .reportDate(today)
                .yesterdayTasks("Tarefas de ontem")
                .todayTasks("Tarefas de hoje")
                .impediments("Sem impedimentos")
                .remoteWork(true)
                .user(testUser)
                .build();
    }

    @Test
    void saveDailyReport_ShouldSaveAndReturnReport() {
        // Arrange
        when(dailyReportRepository.save(any(DailyReport.class))).thenReturn(testReport);

        // Act
        DailyReport result = dailyReportService.saveDailyReport(testReport);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(today, result.getReportDate());
        assertEquals("Tarefas de ontem", result.getYesterdayTasks());
        assertEquals("Tarefas de hoje", result.getTodayTasks());
        assertEquals("Sem impedimentos", result.getImpediments());
        assertTrue(result.getRemoteWork());
        assertEquals(testUser, result.getUser());
        verify(dailyReportRepository, times(1)).save(testReport);
    }

    @Test
    void getRecentReports_ShouldReturnListOfReports() {
        // Arrange
        DailyReport yesterdayReport = DailyReport.builder()
                .id(2L)
                .reportDate(today.minusDays(1))
                .yesterdayTasks("Tarefas de anteontem")
                .todayTasks("Tarefas de ontem")
                .user(testUser)
                .build();

        List<DailyReport> reports = List.of(testReport, yesterdayReport);
        when(dailyReportRepository.findTop2ByUserOrderByReportDateDesc(testUser)).thenReturn(reports);

        // Act
        List<DailyReport> result = dailyReportService.getRecentReports(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(today, result.get(0).getReportDate());
        assertEquals(today.minusDays(1), result.get(1).getReportDate());
        verify(dailyReportRepository, times(1)).findTop2ByUserOrderByReportDateDesc(testUser);
    }

    @Test
    void getTodaysReport_ExistingReport_ShouldReturnReport() {
        // Arrange
        when(dailyReportRepository.findByUserAndReportDate(testUser, today)).thenReturn(Optional.of(testReport));

        // Act
        Optional<DailyReport> result = dailyReportService.getTodaysReport(testUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(today, result.get().getReportDate());
        verify(dailyReportRepository, times(1)).findByUserAndReportDate(testUser, today);
    }

    @Test
    void getTodaysReport_NoReport_ShouldReturnEmpty() {
        // Arrange
        when(dailyReportRepository.findByUserAndReportDate(testUser, today)).thenReturn(Optional.empty());

        // Act
        Optional<DailyReport> result = dailyReportService.getTodaysReport(testUser);

        // Assert
        assertTrue(result.isEmpty());
        verify(dailyReportRepository, times(1)).findByUserAndReportDate(testUser, today);
    }

    @Test
    void getAllReports_ShouldReturnListOfAllReports() {
        // Arrange
        DailyReport yesterdayReport = DailyReport.builder()
                .id(2L)
                .reportDate(today.minusDays(1))
                .yesterdayTasks("Tarefas de anteontem")
                .todayTasks("Tarefas de ontem")
                .user(testUser)
                .build();

        DailyReport oldReport = DailyReport.builder()
                .id(3L)
                .reportDate(today.minusDays(7))
                .yesterdayTasks("Tarefas antigas")
                .todayTasks("Tarefas da semana passada")
                .user(testUser)
                .build();

        List<DailyReport> reports = List.of(testReport, yesterdayReport, oldReport);
        when(dailyReportRepository.findByUserOrderByReportDateDesc(testUser)).thenReturn(reports);

        // Act
        List<DailyReport> result = dailyReportService.getAllReports(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(dailyReportRepository, times(1)).findByUserOrderByReportDateDesc(testUser);
    }
}
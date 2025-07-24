package api.work.profile.service;

import api.work.profile.entity.DailyReport;
import api.work.profile.entity.User;
import api.work.profile.repository.DailyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportRepository dailyReportRepository;

    public DailyReport saveDailyReport(DailyReport dailyReport) {
        return dailyReportRepository.save(dailyReport);
    }

    public List<DailyReport> getRecentReports(User user) {
        return dailyReportRepository.findTop2ByUserOrderByReportDateDesc(user);
    }

    public Optional<DailyReport> getTodaysReport(User user) {
        return dailyReportRepository.findByUserAndReportDate(user, LocalDate.now()).stream().findFirst();
    }
    
    public Optional<DailyReport> getReportByDate(User user, LocalDate date) {
        return dailyReportRepository.findByUserAndReportDate(user, date).stream().findFirst();
    }

    public List<DailyReport> getAllReports(User user) {
        return dailyReportRepository.findByUserOrderByReportDateDesc(user);
    }
    
    public List<DailyReport> getWeeklyReports(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(30); // Buscar últimos 30 dias
        
        List<DailyReport> reports = dailyReportRepository.findByUserAndReportDateBetweenOrderByReportDateDesc(
            user, startOfWeek, today
        );
        
        return reports != null ? reports : new ArrayList<>();
    }
    
    public Optional<DailyReport> getReportById(Long id, User user) {
        return dailyReportRepository.findByIdAndUser(id, user);
    }
    
    public void deleteReport(DailyReport report) {
        dailyReportRepository.delete(report);
    }
}
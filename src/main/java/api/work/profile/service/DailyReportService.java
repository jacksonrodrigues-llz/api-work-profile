package api.work.profile.service;

import api.work.profile.entity.DailyReport;
import api.work.profile.entity.User;
import api.work.profile.repository.DailyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        return dailyReportRepository.findByUserAndReportDate(user, LocalDate.now());
    }

    public List<DailyReport> getAllReports(User user) {
        return dailyReportRepository.findByUserOrderByReportDateDesc(user);
    }
}
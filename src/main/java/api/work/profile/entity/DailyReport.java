package api.work.profile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "yesterday_tasks", nullable = false, length = 1000)
    private String yesterdayTasks;

    @Column(name = "today_tasks", nullable = false, length = 1000)
    private String todayTasks;

    @Column(name = "impediments", length = 1000)
    private String impediments;

    @Column(name = "remote_work")
    private Boolean remoteWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
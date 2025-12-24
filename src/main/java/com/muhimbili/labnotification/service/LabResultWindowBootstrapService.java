package com.muhimbili.labnotification.service;

import com.muhimbili.labnotification.configation.database.DatabaseRepository;
import com.muhimbili.labnotification.configation.database.entities.LabResultWindow;
import com.muhimbili.labnotification.configation.database.repository.LabResultWindowRepository;
import com.muhimbili.labnotification.utility.LoggerService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabResultWindowBootstrapService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DatabaseRepository databaseRepository;
    private final LoggerService loggerService;

    public LabResultWindowBootstrapService(DatabaseRepository databaseRepository,
                                           LoggerService loggerService) {
        this.databaseRepository = databaseRepository;
        this.loggerService = loggerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadLabResultWindows() {
        List<LabResultWindow> windows =  databaseRepository.getLabResultWindowRepository().findAll();
        long enabledCount = windows.stream().filter(this::isEnabled).count();
        long disabledCount = windows.size() - enabledCount;

        loggerService.info("----> lab_result_windows -> total={}, enabled={}, disabled={}",
                windows.size(), enabledCount, disabledCount);

        List<LabResultWindow> enabledWindows = windows.stream()
                .filter(this::isEnabled)
                .sorted(Comparator.comparing(LabResultWindow::getResultsDate)
                        .thenComparing(LabResultWindow::getFromTime))
                .collect(Collectors.toList());

        if (enabledWindows.isEmpty()) {
            loggerService.warn("----> lab_result_windows -> no enabled windows found");
            return;
        }

        loggerService.info("----> lab_result_windows -> printing {} enabled windows", enabledWindows.size());
        enabledWindows.forEach(window -> loggerService.info(formatWindow(window)));
    }

    private boolean isEnabled(LabResultWindow window) {
        return Boolean.TRUE.equals(window.getEnabled());
    }

    private String formatWindow(LabResultWindow window) {
        return String.format("----> [window #%d] %s %s-%s (interval: %d mins) lastRun=%s",
                window.getId(),
                window.getResultsDate(),
                formatTime(window.getFromTime()),
                formatTime(window.getToTime()),
                window.getIntervalMinutes(),
                window.getLastRunAt());
    }

    private String formatTime(java.time.LocalTime time) {
        return time == null ? "--:--" : TIME_FORMATTER.format(time);
    }
}

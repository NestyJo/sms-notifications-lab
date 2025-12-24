package com.muhimbili.labnotification.service;

import com.muhimbili.labnotification.configation.database.DatabaseRepository;
import com.muhimbili.labnotification.configation.database.entities.LabResultWindow;
import com.muhimbili.labnotification.utility.LoggerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class LabOrdersSchedulerService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final LocalTime WINDOW_START = LocalTime.of(8, 0);
    private static final LocalTime WINDOW_END = LocalTime.of(16, 20);
    private static final LocalTime SCHEDULE_START = LocalTime.of(8, 15);
    private static final LocalTime SCHEDULE_END = LocalTime.of(16, 15);
    private static final int WINDOW_INTERVAL_MINUTES = 10;
    private static final int FETCH_DELAY_MINUTES = 15;

    private final LabOrdersService labOrdersService;
    private final DatabaseRepository databaseRepository;
    private final LabResultWindowBootstrapService labResultWindowBootstrapService;
    private final LoggerService loggerService;

    private LocalTime nextFromTime = WINDOW_START;
    private LocalDate lastRunDate;

    public LabOrdersSchedulerService(LabOrdersService labOrdersService,
                                     DatabaseRepository databaseRepository,
                                     LabResultWindowBootstrapService labResultWindowBootstrapService,
                                     LoggerService loggerService) {
        this.labOrdersService = labOrdersService;
        this.databaseRepository = databaseRepository;
        this.labResultWindowBootstrapService = labResultWindowBootstrapService;
        this.loggerService = loggerService;
    }

    @Scheduled(cron = "0 15/10 8-16 * * *")
    public void scheduleLabOrdersFetch() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (now.isAfter(SCHEDULE_END)) {
            loggerService.info("lab_orders_scheduler -> schedule closed for today ({}), skipping run", today);
            return;
        }

        if (now.isBefore(SCHEDULE_START)) {
            loggerService.info("lab_orders_scheduler -> schedule not open yet for today ({}), skipping run", today);
            return;
        }

        if (lastRunDate == null || !lastRunDate.equals(today)) {
            loggerService.info("lab_orders_scheduler -> resetting windows for {}", today);
            nextFromTime = WINDOW_START;
            lastRunDate = today;
        }

        LocalTime referenceTime = now.minusMinutes(FETCH_DELAY_MINUTES);
        if (referenceTime.isBefore(WINDOW_START)) {
            referenceTime = WINDOW_START;
        }
        LocalTime lastWindowStart = WINDOW_END.minusMinutes(WINDOW_INTERVAL_MINUTES);
        if (referenceTime.isAfter(lastWindowStart)) {
            referenceTime = lastWindowStart;
        }

        if (referenceTime.isAfter(nextFromTime)) {
            long minutesSinceStart = Duration.between(WINDOW_START, referenceTime).toMinutes();
            long windowsPassed = Math.max(0, minutesSinceStart / WINDOW_INTERVAL_MINUTES);
            LocalTime alignedWindowStart = WINDOW_START.plusMinutes(windowsPassed * WINDOW_INTERVAL_MINUTES);
            if (alignedWindowStart.isAfter(lastWindowStart)) {
                alignedWindowStart = lastWindowStart;
            }
            nextFromTime = alignedWindowStart;
        }

        String date = DATE_FORMATTER.format(today);
        LocalTime from = nextFromTime;
        LocalTime to = from.plusMinutes(WINDOW_INTERVAL_MINUTES);

        if (to.isAfter(WINDOW_END)) {
            loggerService.info("lab_orders_scheduler -> reached end of day window, resetting to next day");
            from = WINDOW_START;
            to = WINDOW_START.plusMinutes(WINDOW_INTERVAL_MINUTES);
            nextFromTime = WINDOW_START;
            lastRunDate = today.plusDays(1);
        }

        logWindowPlan(today, from);

        String fromStr = from.toString();
        String toStr = to.toString();

        loggerService.info("lab_orders_scheduler -> triggering fetch window {} {}-{}", date, fromStr, toStr);
        try {
            labOrdersService.fetchOrders(date, fromStr, toStr);
            updateWindowState(date, from, to, to.equals(WINDOW_END));
        } catch (Exception ex) {
            loggerService.error("lab_orders_scheduler -> fetch failed for {} {}-{}: {}", date, fromStr, toStr, ex.getMessage(), ex);
        }

        nextFromTime = to;
    }

    private void logWindowPlan(LocalDate date, LocalTime currentFrom) {
        StringBuilder builder = new StringBuilder("lab_orders_scheduler -> windows for ")
                .append(date)
                .append(':');

        LocalTime pointer = WINDOW_START;
        int index = 1;
        while (pointer.isBefore(WINDOW_END)) {
            LocalTime windowEnd = pointer.plusMinutes(WINDOW_INTERVAL_MINUTES);
            if (windowEnd.isAfter(WINDOW_END)) {
                windowEnd = WINDOW_END;
            }
            boolean current = pointer.equals(currentFrom);
            builder.append(String.format("%n  #%02d %s-%s%s", index, pointer, windowEnd, current ? "  <-- current" : ""));
            pointer = pointer.plusMinutes(WINDOW_INTERVAL_MINUTES);
            index++;
        }
        loggerService.info(builder.toString());
    }

    private void updateWindowState(String date, LocalTime from, LocalTime to, boolean lastWindow) {
        try {
            Optional<LabResultWindow> windowOpt = databaseRepository.findLabResultWindow(date, from);
            LabResultWindow window = windowOpt.orElseGet(() -> {
                LabResultWindow fresh = new LabResultWindow();
                fresh.setCreatedAt(Instant.now());
                fresh.setEnabled(Boolean.TRUE);
                return fresh;
            });
            Instant now = Instant.now();
            window.setResultsDate(date);
            window.setFromTime(from);
            window.setToTime(to);
            window.setIntervalMinutes(WINDOW_INTERVAL_MINUTES);
            window.setLastRunAt(now);
            window.setUpdatedAt(now);
            databaseRepository.saveLabResultWindow(window);

            if (lastWindow) {
                loggerService.info("lab_orders_scheduler -> last window completed, bootstrap windows for next day");
                labResultWindowBootstrapService.loadLabResultWindows();
            }
        } catch (Exception ex) {
            loggerService.error("lab_orders_scheduler -> failed to update window {} {}-{}: {}", date, from, to, ex.getMessage());
        }
    }
}

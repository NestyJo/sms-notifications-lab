package com.muhimbili.labnotification.utility;

import com.muhimbili.labnotification.service.sms.SmsHistoryService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class ScheduleUtility {

    private final LoggerService logger;
    private final SmsHistoryService smsHistoryService;

    public ScheduleUtility(LoggerService logger,
                           SmsHistoryService smsHistoryService) {
        this.logger = logger;
        this.smsHistoryService = smsHistoryService;
    }

    @Scheduled(fixedDelayString = "${sms.scheduler.delay:2000}",
               initialDelayString = "${sms.scheduler.initial-delay:5000}")
    public void runScheduledService() {
        processSmsHistoryBatch();
        dispatchQueuedSms();
    }

    @Scheduled(fixedDelayString = "${batch.scheduler.delay:60000}",
               initialDelayString = "${batch.scheduler.initial-delay:10000}")
    public void processReceivedBatch() {
        // placeholder for other batch processing jobs
    }

    private void processSmsHistoryBatch() {
        try {
            int rows = smsHistoryService.stageSmsHistoryBatch().size();
            if (rows > 0) {
                logger.info("sms_history -> Processing sms notifications");
                logger.info("schedule_sms -> staged {} sms rows", rows);
            }
        } catch (Exception ex) {
            logger.error("schedule_sms -> sms staging failed: {}", ex.getMessage(), ex);
        }
    }

    private void dispatchQueuedSms() {
        try {
            int sent = smsHistoryService.dispatchPendingSms();
            if (sent > 0) {
                logger.info("schedule_sms -> dispatched {} sms rows", sent);
            }
        } catch (Exception ex) {
            logger.error("schedule_sms -> sms dispatch failed: {}", ex.getMessage(), ex);
        }
    }
}

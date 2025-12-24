package com.muhimbili.labnotification.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Minimal Logger Service exposing only the intentionally supported operations.
 */
@Service
public class LoggerService {

    private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);


    public void log(Object object) {
        if (object != null) {
            logger.info("--->>> {} <<<---", object);
        }
    }


    public void log(Object object, String level) {
        if (object == null) {
            return;
        }

        String data = object.toString();
        if (level == null) {
            logger.info(data);
            return;
        }

        switch (level.toUpperCase()) {
            case "DEBUG" -> logger.debug(data);
            case "WARN" -> logger.warn(data);
            case "ERROR" -> logger.error(data);
            case "TRACE" -> logger.trace(data);
            default -> logger.info(data);
        }
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public void trace(String message, Object... args) {
        logger.trace(message, args);
    }
}

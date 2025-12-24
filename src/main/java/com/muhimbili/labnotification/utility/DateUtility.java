package com.muhimbili.labnotification.utility;

import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class DateUtility {
    public final String TRANS_ID_PREFIX = "yyyyMMddHHmmss";
    public final String DATE_INT_FORMAT = "yyyyMMdd";
    public final String DATE_STR_FORMAT = "yyyy-MM-dd";
    public final String DATE_DMY_FORMAT = "dd-MM-yyyy";
    public final String DATE_TIME_DMY_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public final String DATE_SQL_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final String CURRENT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    public final String DATE_DISPLAY_FORMAT = "dd MMM yyyy"; //18 Jun 2019

    public String formatCurrentTime(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public String formatCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_SQL_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public Integer formatTimeToDateInt(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return Integer.parseInt(dateFormat.format(date));
    }

    public Integer formatTimeToDateInt(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_INT_FORMAT);
        return Integer.parseInt(dateFormat.format(date));
    }

    public Integer formatTimeToDateInt() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_INT_FORMAT);
        return Integer.parseInt(dateFormat.format(new Date()));
    }

    public String formatDisplayDate(Date date) {
        return formatDateTime(date, DATE_DISPLAY_FORMAT);
    }

    public Date formatDateIntToDate(int dateInt) {
        try {
            return formatStringToTime(String.valueOf(dateInt), DATE_INT_FORMAT);
        } catch (Exception ignored) {
            return new Date();
        }
    }

    public String formatDateTime(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }


    public String formatDateTime(String format) {
        return formatDateTime(new Date(), format);
    }


    public String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_STR_FORMAT);
        try {
            return formatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public Date formatStringToTime(String dateString, String dateFormat) {
        DateFormat format = new SimpleDateFormat(dateFormat);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public Date formatStringToTime2(String dateString) {
        DateFormat format = new SimpleDateFormat(DATE_SQL_FORMAT);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public Date formatDateFromDisplayDate(String dateString) {
        DateFormat format = new SimpleDateFormat(DATE_DISPLAY_FORMAT);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public Date createTime(int timeUnit, int difference) {
        Calendar cal = Calendar.getInstance();
        cal.add(timeUnit, difference);
        return cal.getTime();
    }

    public Date addTime(Date date, int timeUnit, int difference) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(timeUnit, difference);
        return calendar.getTime();
    }

    public long getTimeDifference(Date endDate, Date startDate, TimeUnit timeUnit) {

        long diff = endDate.getTime() > startDate.getTime() ? endDate.getTime() - startDate.getTime() : startDate.getTime() - endDate.getTime();
        return switch (timeUnit) {
            case SECONDS -> diff / 1000 % 60;
            case DAYS -> diff / (24 * 60 * 60 * 1000);
            case HOURS -> diff / (60 * 60 * 1000) % 24;
            case MINUTES -> diff / (60 * 1000) % 60;
            case MILLISECONDS -> diff;
            default -> 0;
        };
    }

    public String createTime(int timeUnit, int difference, String format) {
        Calendar cal = Calendar.getInstance();
        cal.add(timeUnit, difference);
        return formatDateTime(cal.getTime(), format);
    }

    public void delay(int time) {
        try {
            Thread.sleep(time * 1000L);
        } catch (Exception ignored) {
        }
    }

    public String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat(CURRENT_TIME_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public String generateTimePrefix(){
        return formatCurrentTime(TRANS_ID_PREFIX);
    }

    public LocalDateTime StringyDate(String dateString) {
        // Create a DateTimeFormatter object with the desired date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Parse the date string to obtain a LocalDateTime object
        return LocalDateTime.parse(dateString, formatter);
    }
}

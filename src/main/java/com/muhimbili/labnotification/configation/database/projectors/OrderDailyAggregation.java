package com.muhimbili.labnotification.configation.database.projectors;

import java.time.LocalDate;

public interface OrderDailyAggregation {
    LocalDate getOrderDate();
    long getCount();
}

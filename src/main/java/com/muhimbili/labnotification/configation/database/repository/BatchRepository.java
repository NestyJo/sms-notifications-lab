package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.Batch;
import com.muhimbili.labnotification.configation.database.projectors.BatchProjector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {
    BatchProjector findProjectedById(Long id);

    Optional<Batch> findByResultsDateAndFromTimeAndToTimeAndStatus(LocalDate resultsDate,
                                                                  LocalTime fromTime,
                                                                  LocalTime toTime,
                                                                  String status);
}

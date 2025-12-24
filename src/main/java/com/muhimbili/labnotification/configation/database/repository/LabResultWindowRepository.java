package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.LabResultWindow;
import com.muhimbili.labnotification.configation.database.projectors.LabResultWindowProjector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabResultWindowRepository extends JpaRepository<LabResultWindow, Long> {
    Optional<LabResultWindow> findByResultsDateAndFromTime(String resultsDate, java.time.LocalTime fromTime);

    LabResultWindowProjector findProjectedById(Long id);
}

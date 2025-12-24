package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.Patient;
import com.muhimbili.labnotification.configation.database.projectors.PatientProjector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByMrNumber(String mrNumber);

    PatientProjector findProjectedById(Long id);
}

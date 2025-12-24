package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.LabDepartment;
import com.muhimbili.labnotification.configation.database.projectors.LabDepartmentProjector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabDepartmentRepository extends JpaRepository<LabDepartment, Long> {
    Optional<LabDepartment> findByCodeAndLabCode(String code, String labCode);

    LabDepartmentProjector findProjectedById(Long id);
}

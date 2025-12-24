package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.Profile;
import com.muhimbili.labnotification.configation.database.projectors.ProfileProjector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    ProfileProjector findProjectedById(Long id);

    Optional<Profile> findByOrderIdAndProfileCode(Long orderId, String profileCode);
}

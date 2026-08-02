package com.networq.repo;

import com.networq.entity.Jobs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface JobRepo extends JpaRepository<Jobs, String> {



    Page<Jobs> findByJobNameContainingIgnoreCase(
            String jobName,
            Pageable pageable
    );


    Page<Jobs> findByExpiryDateGreaterThanEqual(
            Instant currentTime,
            Pageable pageable
    );
}

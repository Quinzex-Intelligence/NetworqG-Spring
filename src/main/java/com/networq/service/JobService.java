package com.networq.service;


import com.networq.entity.Jobs;
import com.networq.repo.JobRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JobService {
    private final JobRepo jobRepo;
    public String createJob(Jobs jobs){
         log.info("Creating Job. jobName={}", jobs.getJobName());
         Jobs savedJob = jobRepo.save(jobs);
         log.info("Job created successfully. id={}, jobName={}", savedJob.getId(), savedJob.getJobName());
         return jobs.getJobName() + " is created";
    }

    public String  updateJob(Jobs jobs){
        log.info("Updating Job. jobId={}, jobName={}", jobs.getJobId(), jobs.getJobName());
        Jobs job = jobRepo.findById(jobs.getJobId()).orElseThrow(()->new EntityNotFoundException("Job not found."));
        job.setJobName(jobs.getJobName());
        job.setJobDescription(jobs.getJobDescription());
        job.setExpiryDate(jobs.getExpiryDate());
        jobRepo.save(job);
        log.info("Job updated successfully. id={}, jobName={}", job.getId(), job.getJobName());
        return jobs.getJobName() + " is updated";
    }
    public String  deleteJob(List<String> jobIds){
        log.info("Deleting Jobs. jobCount={}", jobIds.size());
        jobRepo.deleteAllById(jobIds);
        log.info("Jobs deleted successfully. jobCount={}", jobIds.size());
        return "jobs deleted";
    }

    public Page<Jobs> getAllJobs(Pageable pageable){
        log.info("Fetching jobs. page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return jobRepo.findAll(pageable);
    }
    public Page<Jobs> searchJobs(String jobName,int page, int size){
        log.info("Searching jobs. jobName={}, page={}, size={}", jobName, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return jobRepo.findByJobNameContainingIgnoreCase(jobName, pageable);

    }

    public Page<Jobs> getAvailableJobs(Pageable pageable) {
        log.info("Fetching available jobs. page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return jobRepo.findByExpiryDateGreaterThanEqual(
                Instant.now(),
                pageable
        );
    }
}

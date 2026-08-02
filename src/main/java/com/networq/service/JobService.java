package com.networq.service;


import com.networq.entity.Jobs;
import com.networq.repo.JobRepo;
import lombok.RequiredArgsConstructor;
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
public class JobService {
    private final JobRepo jobRepo;
    public String createJob(Jobs jobs){
         jobRepo.save(jobs);
         return jobs.getJobName() + " is created";
    }

    public String  updateJob(Jobs jobs){
        Jobs job = jobRepo.findById(jobs.getJobId()).orElseThrow(()->new RuntimeException("Job not found."));
        job.setJobName(jobs.getJobName());
        job.setJobDescription(jobs.getJobDescription());
        job.setExpiryDate(jobs.getExpiryDate());
        jobRepo.save(job);
        return jobs.getJobName() + " is updated";
    }
    public String  deleteJob(List<String> jobIds){
        jobRepo.deleteAllById(jobIds);
        return "jobs deleted";
    }

    public Page<Jobs> getAllJobs(Pageable pageable){
        return jobRepo.findAll(pageable);
    }
    public Page<Jobs> searchJobs(String jobName,int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return jobRepo.findByJobNameContainingIgnoreCase(jobName, pageable);

    }

    public Page<Jobs> getAvailableJobs(Pageable pageable) {

        return jobRepo.findByExpiryDateGreaterThanEqual(
                Instant.now(),
                pageable
        );
    }
}

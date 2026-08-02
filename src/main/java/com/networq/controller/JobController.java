package com.networq.controller;

import com.networq.entity.Jobs;
import com.networq.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<String> createJob(@RequestBody Jobs jobs) {
        return ResponseEntity.ok(jobService.createJob(jobs));
    }

    @PutMapping
    public ResponseEntity<String> updateJob(@RequestBody Jobs jobs) {
        return ResponseEntity.ok(jobService.updateJob(jobs));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteJob(@RequestBody List<String> jobIds) {
        return ResponseEntity.ok(jobService.deleteJob(jobIds));
    }
    @GetMapping
    public ResponseEntity<Page<Jobs>> getAllJobs(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdDate"
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(jobService.getAllJobs(pageable));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<Jobs>> searchJobs(

            @RequestParam
            String jobName,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                jobService.searchJobs(
                        jobName,
                        page,
                        size
                )
        );
    }
    @GetMapping("/public")
    public ResponseEntity<Page<Jobs>> getAvailableJobs(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                jobService.getAvailableJobs(pageable)
        );
    }
}
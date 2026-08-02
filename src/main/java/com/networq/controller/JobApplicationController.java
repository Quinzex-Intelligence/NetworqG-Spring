package com.networq.controller;

import com.networq.entity.Jobs;
import com.networq.repo.JobRepo;
import com.networq.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobRepo jobRepo;
    private final EmailService emailService;

    @PostMapping(
            value = "/{jobId}/apply",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> applyForJob(
            @PathVariable String jobId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestPart("resume") MultipartFile resume
    ) throws MessagingException, IOException {

        // Check if job exists
        Jobs job = jobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found."));

        // Validate resume
        if (resume.isEmpty()) {
            return ResponseEntity.badRequest().body("Resume is required.");
        }

        if (resume.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("Resume size must not exceed 5 MB.");
        }

        String contentType = resume.getContentType();

        if (contentType == null ||
                !(contentType.equals("application/pdf")
                        || contentType.equals("application/msword")
                        || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {

            return ResponseEntity.badRequest()
                    .body("Only PDF, DOC and DOCX files are allowed.");
        }

        // Send email
        emailService.sendJobApplication(
                job.getJobName(),
                fullName,
                email,
                phone,
                resume
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body("Application submitted successfully.");
    }
}
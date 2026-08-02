package com.networq.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;


    @Value("${company.email}")
    private String companyEmail;
    @Value("${spring.mail.username}")
    private String fromEmail;
    public void sendJobApplication(String jobName, String fullName, String email, String phone, MultipartFile resume) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
        helper.setTo(companyEmail);
        helper.setFrom(fromEmail);
        helper.setSubject("New Job Application - " + jobName);

        String body = """
                Hello,

                A new candidate has applied for the following position.

                ----------------------------------------
                Job Title : %s

                Candidate Details
                ----------------------------------------
                Name  : %s
                Email : %s
                Phone : %s

                The candidate's resume is attached with this email.

                Regards,
                Networq Careers
                """.formatted(
                jobName,
                fullName,
                email,
                phone
        );
        helper.setText(body, false);
        String fileName = resume.getOriginalFilename();
        helper.addAttachment( fileName != null ? fileName : "resume",new ByteArrayResource(resume.getBytes()));
        mailSender.send(mimeMessage);
    }
}

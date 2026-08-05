package com.networq.service;

import com.networq.logging.LoggingUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;


    @Value("${company.email}")
    private String companyEmail;
    @Value("${spring.mail.username}")
    private String fromEmail;
    public void sendJobApplication(String jobName, String fullName, String email, String phone, MultipartFile resume) throws MessagingException, IOException {
        String subject = "New Job Application - " + jobName;
        log.info("Email sending started. recipient={}, subject={}", companyEmail, subject);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
            helper.setTo(companyEmail);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);

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
            log.info("Email sent successfully. recipient={}, subject={}", companyEmail, subject);
        } catch (MessagingException | IOException | RuntimeException ex) {
            log.error("""
                    Email failed.
                    recipient  : {}
                    subject    : {}
                    Stacktrace :
                    {}
                    """,
                    companyEmail,
                    subject,
                    LoggingUtils.stackTrace(ex));
            throw ex;
        }
    }
}

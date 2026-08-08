package com.society.serviceimpl;

import com.society.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Notification emails are best-effort. The action that triggered
            // the email (bill created, notice published, society approved...)
            // has already happened and must not be rolled back just because
            // the mail server is unreachable or misconfigured - we log and
            // move on instead of throwing.
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendToAll(Iterable<String> recipients, String subject, String body) {
        for (String to : recipients) {
            send(to, subject, body);
        }
    }
}

package com.tavinki.taskiu.modules.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.common.exception.EmailVerifyException;
import com.tavinki.taskiu.common.utils.CodeGeneratorUtils;
import com.tavinki.taskiu.modules.email.repository.VerificationCodeRepository;
import com.tavinki.taskiu.modules.user.service.UserService;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger customLogger = LoggerFactory.getLogger(EmailService.class);

    private final VerificationCodeRepository verificationCodeRepository;

    private final TemplateEngine templateEngine;

    private final JavaMailSender mailSender;

    private final UserService userService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(@NonNull String toEmail) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String code = CodeGeneratorUtils.generateSecureCode();
            verificationCodeRepository.save(toEmail, Objects.requireNonNull(code), 5);
            helper.setFrom(Objects.requireNonNull(fromEmail));
            helper.setTo(toEmail);
            helper.setSubject("【Taskiu】your verification code");
            Context context = new Context();
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("email-verification", context);

            helper.setText(Objects.requireNonNull(htmlContent), true);

            mailSender.send(message);
            customLogger.info("HTML verification email sent to: {}", toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new EmailVerifyException("Failed to send email", e);
        }
    }

    public User verifyProcess(@NonNull String toEmail, @NonNull String inputCode) {

        customLogger.info("Verifying code for email: {}, code: {}", toEmail, inputCode);
        if (!verificationCodeRepository.verify(toEmail, inputCode))
            return null;
        verificationCodeRepository.delete(toEmail);

        return userService.markEmailAsVerified(toEmail);
    }

    public boolean limitingSendEmail(@NonNull String toEmail) {
        return verificationCodeRepository.isRateLimited(toEmail);
    }

}

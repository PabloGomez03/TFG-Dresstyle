package com.dresstyle.service;

import com.dresstyle.dto.OrderPlacedEvent;
import com.dresstyle.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean emailEnabled;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${notification.from:no-reply@dresstyle.com}") String fromAddress,
                                    @Value("${notification.email.enabled:false}") boolean emailEnabled) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.emailEnabled = emailEnabled;
    }

    public void notifyUserRegistered(UserRegisteredEvent event) {
        String subject = "Bienvenido a Dresstyle";
        String body = String.format("Hola %s,%n%nGracias por registrarte en Dresstyle.%nTu email de acceso es: %s.%n%nSaludos,%nEquipo Dresstyle",
                event.getNombre(), event.getEmail());
        sendEmail(event.getEmail(), subject, body);
    }

    public void notifyOrderPlaced(OrderPlacedEvent event) {
        StringBuilder itemsSummary = new StringBuilder();
        if (event.getItems() != null) {
            event.getItems().forEach(item -> itemsSummary.append(String.format("- %s x%d (%s): %.2f€%n",
                    item.getName(), item.getQuantity(), item.getSize(), item.getPrice())));
        }
        String subject = "Pedido recibido en Dresstyle";
        String body = String.format("Hola,%n%nTu pedido %s ha sido recibido con éxito.%nEstado: %s.%nTotal: %.2f€.%n%nDetalles:%n%s%nGracias por comprar con nosotros.%n%nSaludos,%nEquipo Dresstyle",
                event.getOrderId(), event.getStatus(), event.getTotal(), itemsSummary);
        sendEmail(event.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("[NotificationService] Email disabled. Notification to {} skipped. Subject: {}", to, subject);
            log.info("[NotificationService] Email body:\n{}", body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[NotificationService] Email enviado a {}", to);
        } catch (Exception ex) {
            log.error("[NotificationService] Error enviando email a {}", to, ex);
        }
    }
}

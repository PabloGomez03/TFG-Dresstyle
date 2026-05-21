package com.dresstyle.component;

import com.dresstyle.dto.OrderPlacedEvent;
import com.dresstyle.dto.UserRegisteredEvent;
import com.dresstyle.service.EmailNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class NotificationListener {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    public NotificationListener(ObjectMapper objectMapper,
                                EmailNotificationService emailNotificationService) {
        this.objectMapper = objectMapper;
        this.emailNotificationService = emailNotificationService;
    }

    @RabbitListener(queues = "notificationQueue")
    public void handleNotification(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            if ("registrationRoutingKey".equals(routingKey)) {
                UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);
                emailNotificationService.notifyUserRegistered(event);
                return;
            }

            if ("orderRoutingKey".equals(routingKey)) {
                OrderPlacedEvent event = objectMapper.readValue(payload, OrderPlacedEvent.class);
                emailNotificationService.notifyOrderPlaced(event);
                return;
            }

            log.warn("[NotificationService] Evento recibido en cola notificationQueue con routingKey desconocido: {}", routingKey);
        } catch (Exception ex) {
            log.error("[NotificationService] Error procesando mensaje de notificación", ex);
        }
    }
}

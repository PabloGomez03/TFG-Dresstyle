package com.dresstyle.component;

import com.dresstyle.dto.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    @RabbitListener(queues = "notificationQueue") //Escucha la cola creada en authservice
    public void handleNotification(UserRegisteredEvent event) {
        // Queda en el aire si implementar aqui la api de emails para produccion
        System.out.println("LOG: Enviando notificación al usuario -> " + event.getNombre());
    }
}

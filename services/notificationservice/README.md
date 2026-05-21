# Notification Service

Servicio de notificaciones por email basado en eventos de RabbitMQ.

## Configuración

### Variables de Entorno Requeridas

```bash
# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_USERNAME=admin
SPRING_RABBITMQ_PASSWORD=admin123

# Email (obligatorio si NOTIFICATION_EMAIL_ENABLED=true)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
NOTIFICATION_FROM=your-email@gmail.com

# Service Discovery
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-server:8761/eureka/

# Feature Flags
NOTIFICATION_EMAIL_ENABLED=true
```

### Nota sobre Gmail

- Usar **App Password**, no la contraseña regular
- Generar en: https://myaccount.google.com/apppasswords
- Requiere 2FA habilitado en la cuenta

## Eventos Escuchados

- **registrationRoutingKey**: Notificación de registro de usuario
- **orderRoutingKey**: Notificación de pedido recibido

## Desarrollo Local

1. Copiar variables de `.env.example` a `.env`
2. Ejecutar `docker-compose up`
3. Acceder a `/actuator/health` en puerto 8084

## Seguridad

⚠️ **IMPORTANTE**: 
- Nunca versionar `.env` con credenciales reales
- Usar Docker secrets o variables de entorno en producción
- Cambiar credenciales por defecto de RabbitMQ

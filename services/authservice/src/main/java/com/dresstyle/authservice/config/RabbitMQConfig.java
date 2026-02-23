package com.dresstyle.authservice.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;

@Configuration
public class RabbitMQConfig {

        @Bean
        public Queue notificationQueue() { return new Queue("notificationQueue"); }

        @Bean
        public TopicExchange exchange() { return new TopicExchange("notificationExchange"); }

        @Bean
        public Binding binding(Queue queue, TopicExchange exchange) {
            return BindingBuilder.bind(queue).to(exchange).with("registrationRoutingKey");
        }

        @Bean
        public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
        }

}

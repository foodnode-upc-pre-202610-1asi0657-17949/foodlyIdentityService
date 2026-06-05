package com.FoodlyIdentityService.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.FoodlyIdentityService.application.dto.UserCreatedEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import jakarta.jms.TextMessage;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    public static final String EVENT_TYPE_USER_CREATED  = "USER_CREATED";
    public static final String EVENT_TYPE_USER_UPDATED  = "USER_UPDATED";
    private static final String QUEUE_NAME = "IdentityUsersQueue";

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public UserEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void publishUserCreatedEvent(UserCreatedEventDto event) {
        publishEvent(event, EVENT_TYPE_USER_CREATED);
    }

    public void publishUserUpdatedEvent(UserCreatedEventDto event) {
        publishEvent(event, EVENT_TYPE_USER_UPDATED);
    }

    private void publishEvent(UserCreatedEventDto event, String eventType) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);

            log.info("[UserEventPublisher] Publicando evento '{}' para userId={}",
                     eventType, event.getUserId());
            log.debug("[UserEventPublisher] Payload JSON: {}", jsonPayload);

            jmsTemplate.send(QUEUE_NAME, session -> {
                TextMessage message = session.createTextMessage(jsonPayload);
                message.setStringProperty("eventType", eventType);
                message.setStringProperty("sourceService", "foodly-identity-service");
                message.setStringProperty("userId", event.getUserId());
                return message;
            });

            log.info("[UserEventPublisher] Evento '{}' enviado exitosamente a la cola '{}'.",
                     eventType, QUEUE_NAME);

        } catch (JsonProcessingException e) {
            log.error("[UserEventPublisher] Error al serializar el evento a JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Error al serializar evento de usuario a JSON", e);
        }
    }
}

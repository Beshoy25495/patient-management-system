package com.bwagih.patientservice.kafka;

import com.bwagih.patientservice.model.Patient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import events.GenericEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send a Protocol Buffers-based event
     */
    public <T extends Message> void sendEvent(T payload, String eventType, String topic) {
        GenericEvent event = GenericEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(eventType)
                .setTimestamp(Instant.now().toEpochMilli())
                .setPayload(Any.pack(payload)) // <-- Wraps the message like <T>
                .build();

        try {
            kafkaTemplate.send(topic, event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending event: {}, on topic: {}, error: {}", event, topic, e.getMessage(), e);
        }
    }



    /**
     * Send a JSON-based event by wrapping the JSON string inside a Protobuf Any payload.
     */
    public void sendEvent(Object payload, String eventType, String topic) {
        try {
            // Convert POJO to JSON string
            String jsonString = objectMapper.writeValueAsString(payload);

            // Wrap JSON inside a StringValue and pack it into Any
            Any jsonWrapped = Any.pack(StringValue.of(jsonString));


            GenericEvent event = GenericEvent.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setEventType(eventType)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .setPayload(jsonWrapped)
                    .build();

            kafkaTemplate.send(topic, event.toByteArray());

            log.info("JSON event sent successfully. Type: {}, Topic: {}", eventType, topic);
        } catch (Exception e) {
            log.error("Error sending JSON event. Type: {}, Topic: {}, Error: {}", eventType, topic, e.getMessage(), e);
        }
    }


}
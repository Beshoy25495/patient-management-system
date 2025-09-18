package com.bwagih.analyticsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import events.GenericEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class PatientConsumer implements HandlingConsumer {
    private static final Logger log = LoggerFactory.getLogger(PatientConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = handleEvent(event);
            // ... perform any business related to analytics here e.g. save to database for analytics reporting

            log.info("Received Patient Event: [PatientId={},PatientName={},PatientEmail={}]",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing event {}", e.getMessage());
        }
    }

    @Override
    public PatientEvent handleEvent(byte[] genericEvent) throws Exception {

        GenericEvent event = GenericEvent.parseFrom(genericEvent);
        log.info("Received event: [EventId={},EventType={}, Timestamp={} ]",
                event.getEventId(),
                event.getEventType(),
                event.getTimestamp());

        if (!event.hasPayload()) {
            log.warn("Received event with no payload: {}", event);
            return null;
        }

        if (event.getPayload().is(StringValue.class)) {
            String jsonString = event.getPayload().unpack(StringValue.class).getValue();
            return objectMapper.readValue(jsonString, PatientEvent.class);
        } else if (event.getPayload().is(PatientEvent.class)) {
            return event.getPayload().unpack(PatientEvent.class);
        }
        return null;
    }


}

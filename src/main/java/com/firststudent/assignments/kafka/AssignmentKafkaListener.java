package com.firststudent.assignments.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.assignments.outbound.ExternalAssignmentNotifier;
import com.firststudent.assignments.service.AssignmentIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class AssignmentKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(AssignmentKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final AssignmentIngestService ingestService;
    private final ExternalAssignmentNotifier notifier;

    public AssignmentKafkaListener(
            ObjectMapper objectMapper,
            AssignmentIngestService ingestService,
            ExternalAssignmentNotifier notifier) {
        this.objectMapper = objectMapper;
        this.ingestService = ingestService;
        this.notifier = notifier;
    }

    @KafkaListener(
            topics = "#{@assignmentKafkaTopic.name()}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "assignmentKafkaListenerContainerFactory")
    public void onMessage(@Payload String rawJson, Acknowledgment ack) {
        try {
            AssignmentEvent event = objectMapper.readValue(rawJson, AssignmentEvent.class);
            ingestService.apply(event);
            notifier.notifyApplied(event);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON; leaving uncommitted for retry or DLQ wiring: {}", e.toString());
            throw new IllegalArgumentException("Bad assignment event payload", e);
        } catch (RuntimeException e) {
            log.error("Failed processing assignment event: {}", e.toString());
            throw e;
        }
    }
}

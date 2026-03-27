package com.firststudent.assignments.outbound;

import com.firststudent.assignments.domain.Assignment;
import com.firststudent.assignments.kafka.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ExternalAssignmentNotifier {

    private static final Logger log = LoggerFactory.getLogger(ExternalAssignmentNotifier.class);

    private final org.springframework.web.reactive.function.client.WebClient webClient;
    private final boolean enabled;

    public ExternalAssignmentNotifier(
            org.springframework.web.reactive.function.client.WebClient assignmentOutboundWebClient,
            @Value("${app.outbound.enabled:true}") boolean enabled) {
        this.webClient = assignmentOutboundWebClient;
        this.enabled = enabled;
    }

    /**
     * Best-effort notify downstream after Redis has been updated. Failures propagate so the Kafka offset
     * is not committed and the consumer can retry (pair with idempotent downstream or dedupe keys).
     */
    public void notifyApplied(AssignmentEvent event) {
        if (!enabled) {
            log.debug("app.outbound.enabled=false; skipping downstream notification");
            return;
        }
        Assignment payload = event.toAssignment();
        Map<String, Object> body = Map.of(
                "taskId", payload.taskId(),
                "driverId", payload.driverId(),
                "vehicleId", payload.vehicleId(),
                "operation", event.resolveOperation().name(),
                "updatedAt", payload.updatedAt().toString());

        webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.warn("Outbound returned {} — will fail consume so Kafka retries: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.error(ex);
                })
                .onErrorResume(Throwable.class, ex -> {
                    if (!(ex instanceof WebClientResponseException)) {
                        log.warn("Outbound unreachable — will fail consume so Kafka retries: {}", ex.toString());
                    }
                    return Mono.error(ex);
                })
                .block();
    }
}

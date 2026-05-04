package io.cloudops.incidentservice;


import io.cloudops.event.IncidentCreatedEvent;

import io.cloudops.incidentservice.kafka.IncidentEventProducer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class IncidentEventProducerCircuitBreakerTest {

    @Autowired
    private IncidentEventProducer producer;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private IncidentCreatedEvent buildEvent(Long id) {
        return IncidentCreatedEvent.builder()
                .incidentId(id)
                .title("Test incident")
                .reportedBy("user-test")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void circuitBreaker_shouldOpenAfterFailures() {
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(kafkaTemplate).send(anyString(), anyString(), any());

        for (int i = 0; i < 10; i++) {
            try { producer.sendIncidentCreated(buildEvent(1L)); } catch (Exception ignored) {}
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("kafkaProducer");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void circuitBreaker_shouldCallFallbackWhenOpen() {
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(kafkaTemplate).send(anyString(), anyString(), any());

        for (int i = 0; i < 15; i++) {
            assertDoesNotThrow(() -> producer.sendIncidentCreated(buildEvent(2L)));
        }
    }

    @Test
    void circuitBreaker_shouldSucceedWhenKafkaAvailable() {
        doNothing().when(kafkaTemplate).send(anyString(), anyString(), any());

        assertDoesNotThrow(() -> producer.sendIncidentCreated(buildEvent(3L)));

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("kafkaProducer");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
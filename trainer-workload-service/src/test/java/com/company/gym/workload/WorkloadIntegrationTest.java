package com.company.gym.workload;

import com.company.gym.workload.config.JmsConfig;
import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.util.Date;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Full JMS Integration Test (ActiveMQ + MongoDB)")
public class WorkloadIntegrationTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should process message from ActiveMQ and update MongoDB")
    void fullFlow_JmsToMongo() {
        String username = "integration.trainer";
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Int")
                .trainerLastName("Test")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(120)
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();

        jmsTemplate.convertAndSend(JmsConfig.TRAINER_WORKLOAD_QUEUE, request);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var workload = repository.findByUsername(username);
            assertTrue(workload.isPresent(), "Workload should be saved to MongoDB");
            assertTrue(workload.get().getYears().size() > 0, "Years summary should be populated");
        });
    }
}
package com.company.gym.workload.cucumber;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.awaitility.Awaitility;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkloadJmsIntegrationSteps {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Given("the workload database is completely clear")
    public void clearDb() {
        repository.deleteAll();
    }

    @When("a workload message for trainer {string} with duration {int} minutes is sent to the queue")
    public void sendMessageToQueue(String username, int duration) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setIsActive(true);
        request.setTrainingDate(new Date());
        request.setTrainingDuration(duration);
        request.setActionType(TrainerWorkloadRequest.ActionType.ADD);
        request.setTransactionId(UUID.randomUUID().toString());

        jmsTemplate.convertAndSend("trainer-workload-queue", request);
    }

    @Then("the workload service should eventually save the record for {string}")
    public void verifyDbEventually(String username) {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertTrue(repository.findByUsername(username).isPresent(),
                            "The record should appear in the DB after async processing");
                });
    }
}
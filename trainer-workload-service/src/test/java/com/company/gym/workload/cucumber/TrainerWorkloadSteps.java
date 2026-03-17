package com.company.gym.workload.cucumber;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.model.TrainerWorkload;
import com.company.gym.workload.repository.TrainerWorkloadRepository;
import com.company.gym.workload.service.TrainerWorkloadService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerWorkloadSteps {

    @Autowired
    private TrainerWorkloadService workloadService;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Given("the database is clear")
    public void theDatabaseIsClear() {
        repository.deleteAll();
    }

    @When("the system receives a workload request for trainer {string} with duration {int} minutes for date {string}")
    public void theSystemReceivesAWorkloadRequest(String username, int duration, String dateString) throws java.text.ParseException {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = sdf.parse(dateString);

        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName("Lera");
        request.setTrainerLastName("Test");
        request.setIsActive(true);
        request.setTrainingDate(date);
        request.setTrainingDuration(duration);
        request.setActionType(TrainerWorkloadRequest.ActionType.ADD);
        request.setTransactionId(UUID.randomUUID().toString());

        workloadService.updateWorkload(request);
    }

    @Then("the database should contain a workload record for {string}")
    public void theDatabaseShouldContainRecord(String username) {
        Optional<TrainerWorkload> workload = repository.findByUsername(username);
        assertTrue(workload.isPresent(), "Workload profile should be created in DB");
    }

    @And("the total duration for trainer {string} in year {int} and month {int} should be {int} minutes")
    public void checkTotalDuration(String username, int year, int month, int expectedDuration) {
        TrainerWorkload workload = repository.findByUsername(username).orElseThrow();

        long actualDuration = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonthValue() == month)
                .findFirst()
                .map(m -> m.getTotalDuration())
                .orElse(0L);

        assertEquals(expectedDuration, actualDuration, "Duration does not match expected value");
    }



    @When("the system receives a workload request without a trainer username")
    public void theSystemReceivesAWorkloadRequestWithoutUsername() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainingDuration(60);
        request.setActionType(TrainerWorkloadRequest.ActionType.ADD);

        try {
            workloadService.updateWorkload(request);
        } catch (Exception e) {
            System.out.println("Expected error caught: " + e.getMessage());
        }
    }

    @Then("the database should remain empty")
    public void theDatabaseShouldRemainEmpty() {
        assertEquals(0, repository.count(), "Database should be empty because the request was invalid");
    }
}
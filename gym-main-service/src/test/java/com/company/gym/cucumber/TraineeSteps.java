package com.company.gym.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TraineeSteps {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;

    @Given("the gym database is running")
    public void theGymDatabaseIsRunning() {

    }

    @When("a valid user requests the training types list")
    public void aValidUserRequestsTrainingTypes() throws Exception {
        resultActions = mockMvc.perform(get("/api/v1/training-types")
                .with(user("admin").roles("USER")));
    }

    @Then("the system should respond with success")
    public void theSystemShouldRespondWithSuccess() throws Exception {
        resultActions.andExpect(status().is2xxSuccessful());
    }
}
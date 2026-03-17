package com.company.gym.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecuritySteps {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;

    @Given("the user is not authenticated")
    public void theUserIsNotAuthenticated() {
    }

    @When("the user attempts to get training types without a token")
    public void attemptToAccessProtectedEndpoint() throws Exception {
        resultActions = mockMvc.perform(get("/api/v1/training-types"));
    }

    @Then("the system should respond with status code {int} Forbidden")
    public void verifyStatusCode(int expectedStatus) throws Exception {
        resultActions.andExpect(status().is(expectedStatus));
    }
}
Feature: API Component Management
  As a Gym User
  I want to access the gym API
  So that I can retrieve data

  @component @positive
  Scenario: Successfully access protected endpoint with valid user
    Given the gym database is running
    When a valid user requests the training types list
    Then the system should respond with success
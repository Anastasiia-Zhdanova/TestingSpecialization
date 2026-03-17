Feature: Trainer Workload Component Tests
  As a part of the Gym CRM system
  I want to process workload requests
  So that trainers' working hours are updated in the database

  @positive @workload-service
  Scenario: Successfully add new workload to a trainer
    Given the database is clear
    When the system receives a workload request for trainer "lera.test" with duration 60 minutes for date "2026-03-15"
    Then the database should contain a workload record for "lera.test"
    And the total duration for trainer "lera.test" in year 2026 and month 3 should be 60 minutes

  @negative @workload-service
    Scenario: Fail to process workload due to missing trainer username
      Given the database is clear
      When the system receives a workload request without a trainer username
      Then the database should remain empty
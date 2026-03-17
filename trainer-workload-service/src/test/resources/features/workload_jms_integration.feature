Feature: Trainer Workload JMS Integration
  As a Main Service
  I want to send workload messages to the ActiveMQ queue
  So that the Workload Service processes them asynchronously

  @integration @jms
  Scenario: Asynchronous workload processing via message broker
    Given the workload database is completely clear
    When a workload message for trainer "john.doe" with duration 45 minutes is sent to the queue
    Then the workload service should eventually save the record for "john.doe"
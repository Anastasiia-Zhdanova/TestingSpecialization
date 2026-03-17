Feature: Non-Functional Requirements - Security and Permissions
  As a system administrator
  I want to secure all API endpoints
  So that unauthorized users cannot access gym data

  @security @nfr @negative
  Scenario: Deny access to protected endpoint without JWT token
    Given the user is not authenticated
    When the user attempts to get training types without a token
    Then the system should respond with status code 403 Forbidden
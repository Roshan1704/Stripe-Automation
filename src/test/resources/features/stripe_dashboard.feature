@ui @smoke
Feature: Stripe Dashboard Payment Validation

  Scenario: Validate payments page and payment search
    Given the user is logged into Stripe dashboard
    When the user opens payments page
    Then payments page should be displayed
    And the user searches payment id "pi_test_123"
    Then refund status "succeeded" should be visible

  @regression
  Scenario: Validate filters and pagination in payments page
    Given the user is logged into Stripe dashboard
    When the user opens payments page
    Then filters and pagination are available

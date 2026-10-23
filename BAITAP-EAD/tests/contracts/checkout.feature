Feature: Checkout and cancellation contract
  As a customer I want an atomic order and inventory update

  Scenario: Checkout succeeds
    Given I have a valid customer Bearer session
    And a product has stock 10 and unit price 750000
    When I submit an order for 2 units belonging to my customer ID
    Then the API returns 201 with a Location header
    And the order status is PENDING and total is 1500000
    And product stock is 8

  Scenario: A later line fails and all stock rolls back
    Given the first product has stock 10 and the second product has stock 0
    When I checkout one unit of each in the same request
    Then the API returns 422 with code ERR_STOCK_EMPTY
    And the first product stock is still 10
    And no order was created

  Scenario: Repeated cancellation is safe
    Given an order is PENDING and its stock was reserved
    When its owner cancels the order twice
    Then both requests return 200 and stock is restored only once

  Scenario: An order cannot skip payment
    Given an order is PENDING
    When an administrator changes its status to SHIPPED
    Then the API returns 422

  Scenario: Cross-customer data is protected
    Given I am signed in as customer A
    When I read an order owned by customer B
    Then the API returns 403

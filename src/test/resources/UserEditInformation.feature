Feature: Edit User Details
  As a user
  I want to be able to change my username and password

  Background:
    Given database is empty
    And user with id 1234 exists and has username "user", password "secure_password"
    And accessToken with value "accessToken" exists for user 1234

  Scenario: Successful change of username
    When user requests change of username with value "kangaroo" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User successfully updated."
    And response contains key "status" and value "Created"
    And response status code is 201

  Scenario: Successful change of password
    When user requests change of password with value "pig-system" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User successfully updated."
    And response contains key "status" and value "Created"
    And response status code is 201

  Scenario: Failed change of username; new username already assigned
    Given user with id 1235 exists and has username "kangaroo", password "secure_password"
    When user requests change of username with value "kangaroo" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User could not get updated. Username already taken."
    And response status code is 409
    And response contains key "status" and value "Conflict"

  Scenario: Failed change of password; new password contains username
    When user requests change of password with value "user123" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User could not get updated. Username must not appear in password."
    And response status code is 409
    And response contains key "status" and value "Conflict"
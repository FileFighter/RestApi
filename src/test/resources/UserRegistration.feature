Feature: User Registration
  As a user (/admin)
  I want to be able to register (users) with username and password

  Background:
    Given database is empty
    And user with id 1234 exists and has username "user", password "Secure_password1"
    And accessToken with value "accessToken" exists for user 1234
    And user with id 1234 is in group with id 1

  Scenario: Failed registration because password does not match requirements.
    When user requests registration with username "kangaroo", password "short" and password confirmation "short" with accessToken "accessToken"
    Then response status code is 409
    And response contains key "message" and value "User could not be registered. Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number."
    And response contains key "status" and value "conflict"

  Scenario: Successful registration with username, password and password confirmation.
    When user requests registration with username "kangaroo", password "Pig-system12" and password confirmation "Pig-system12" with accessToken "accessToken"
    Then response status code is 201
    And response contains key "message" and value "User successfully created."
    And response contains key "status" and value "created"

  Scenario: Successful registration with username, password and password confirmation; password matches password of other users.
    When user requests registration with username "kangaroo", password "Secure_password1" and password confirmation "Secure_password1" with accessToken "accessToken"
    Then response status code is 201
    And response contains key "message" and value "User successfully created."
    And response contains key "status" and value "created"

  Scenario: Failed registration with used username, arbitrary password and password confirmation.
    When user requests registration with username "user", password "Pig-system12" and password confirmation "Pig-system12" with accessToken "accessToken"
    Then response status code is 409
    And response contains key "message" and value "User could not be registered. Username already taken."
    And response contains key "status" and value "conflict"

  Scenario: Failed registration with used username (other case), arbitrary password and password confirmation.
    When user requests registration with username "User", password "Pig-system12" and password confirmation "Pig-system12" with accessToken "accessToken"
    Then response status code is 409
    And response contains key "message" and value "User could not be registered. Username already taken."
    And response contains key "status" and value "conflict"

  Scenario: Failed registration with username, password and deviating password confirmation.
    When user requests registration with username "kangaroo", password "Pig-system12" and password confirmation "I-love-capitalism420" with accessToken "accessToken"
    Then response status code is 409
    And response contains key "message" and value "User could not be registered. Passwords do not match."
    And response contains key "status" and value "conflict"

  Scenario: Failed registration with username, password and password confirmation; username is part of password.
    When user requests registration with username "kangaroo", password "Kangaroo-system1" and password confirmation "Kangaroo-system1" with accessToken "accessToken"
    Then response status code is 409
    And response contains key "message" and value "User could not be registered. Username must not appear in password."
    And response contains key "status" and value "conflict"

  Scenario: Failed registration with username, password and password confirmation; not in group ADMIN
    Given user 1236 exists
    And user with id 1236 is in group with id -1
    And accessToken with value "wrongAccessToken" exists for user 1236
    When user requests registration with username "kangaroo", password "Pig-system12" and password confirmation "Pig-system12" with accessToken "wrongAccessToken"
    Then response status code is 401
    And response contains key "message" and value "User could not be authenticated. Not in necessary group."
    And response contains key "status" and value "unauthorized"
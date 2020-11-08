#Feature: User Registration
#  As a user (/admin)
#  I want to be able to register (users) with username and password
#
#  Background:
#    Given database is empty
#    And user with id 1234 exists and has username "user", password "secure_password"
#    And accessToken with value "accessToken" exists for user 1234
#    And user with id 1234 is in group with id 1
#
#  Scenario: Successful registration with username, password and password confirmation.
#    When user requests registration with username "kangaroo", password "pig-system" and password confirmation "pig-system" with accessToken "accessToken"
#    Then response status code is 201
#    And response contains key "message" and value "User successfully created."
#    And response contains key "status" and value "created"
#
#  Scenario: Successful registration with username, password and password confirmation; password matches password of other users.
#    When user requests registration with username "kangaroo", password "secure_password" and password confirmation "secure_password" with accessToken "accessToken"
#    Then response status code is 201
#    And response contains key "message" and value "User successfully created."
#    And response contains key "status" and value "created"
#
#  Scenario: Failed registration with used username, arbitrary password and password confirmation.
#    When user requests registration with username "user", password "pig-system" and password confirmation "pig-system" with accessToken "accessToken"
#    Then response status code is 409
#    And response contains key "message" and value "User already exists."
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed registration with used username (other case), arbitrary password and password confirmation.
#    When user requests registration with username "User", password "pig-system" and password confirmation "pig-system" with accessToken "accessToken"
#    Then response status code is 409
#    And response contains key "message" and value "User already exists."
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed registration with username, password and deviating password confirmation.
#    When user requests registration with username "kangaroo", password "pig-system" and password confirmation "i-love-capitalism" with accessToken "accessToken"
#    Then response status code is 409
#    And response contains key "message" and value "Passwords do not match."
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed registration with username, password and password confirmation; username is part of password.
#    When user requests registration with username "kangaroo", password "kangaroo-system" and password confirmation "kangaroo-system" with accessToken "accessToken"
#    Then response status code is 409
#    And response contains key "message" and value "Username must not appear in password."
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed registration with username, password and password confirmation; password appears in list of top 10k passwords
#    When user requests registration with username "kangaroo", password "vietnam" and password confirmation "vietnam" with accessToken "accessToken"
#    Then response status code is 409
#    And response contains key "message" and value "Password must not appear in the top 10000 most common passwords."
#    And response contains key "status" and value "conflict"
#    #https://github.com/iryndin/10K-Most-Popular-Passwords/blob/master/passwords.txt
#
#  Scenario: Failed registration with username, password and password confirmation; not in group ADMIN
#    Given user with id 1236 exists
#    And user with id 1236 is in group with id -1
#    And accessToken with value "wrongAccessToken" exists for user 1236
#    When user requests registration with username "kangaroo", password "pig-system" and password confirmation "pig-system" with accessToken "wrongAccessToken"
#    Then response status code is 401
#    And response contains key "message" and value "User must not register new users."
#    And response contains key "status" and value "unauthorized"
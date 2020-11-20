#Feature: Edit User Details
#  As a user
#  I want to be able to change my username and password
#
#  Background:
#    Given database is empty
#    And user with id 1234 exists and has username "user", password "secure_password"
#    And accessToken with value "accessToken" exists for user 1234
#
#  Scenario: Successful change of username
#    When user requests change of username with value "kangaroo" and accessToken "accessToken"
#    Then response contains key "message" and value "Username successfully changed."
#    And response contains key "status" and value ""
#    And response status code is 201
#
#  Scenario: Successful change of password
#    When user requests change of password with value "pig-system" and accessToken "accessToken" and id "1234"
#    Then response contains key "message" and value "Password successfully changed."
#    And response status code is 201
#
#  Scenario: Failed change of username; new username equals old username
#    When user requests change of username with value "user" and accessToken "accessToken"
#    Then response contains key "message" and value "No changes."
#    And response status code is 409
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed change of username; new username already assigned
#    Given user with id 1235 exists and has username "kangaroo", password "secure_password"
#    When user requests change of username with value "kangaroo" and accessToken "accessToken"
#    Then response contains key "message" and value "Username already assigned."
#    And response status code is 409
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed change of password; new password equals old password
#    When user requests change of password with value "secure_password" and accessToken "accessToken" and id "1234"
#    Then response contains key "message" and value "No changes."
#    And response status code is 409
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed change of password; new password contains username
#    When user requests change of password with value "user123" and accessToken "accessToken" and id "1234"
#    Then response contains key "message" and value "Username must not appear in password."
#    And response status code is 409
#    And response contains key "status" and value "conflict"
#
#  Scenario: Failed change of password; new password appears in list of top 10k passwords
#    When user requests change of password with value "vietnam" and accessToken "accessToken" and id "1234"
#    Then response status code is 409
#    And response contains key "message" and value "Password must not appear in the top 10000 most common passwords."
#    And response contains key "status" and value "conflict"
#
#    #https://github.com/iryndin/10K-Most-Popular-Passwords/blob/master/passwords.txt
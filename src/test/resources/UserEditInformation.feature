Feature: Edit User Details
  As a user
  I want to be able to change my username and password

  Background:
    Given database is empty
    And user with userId 1234 exists and has username "user", password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126" and refreshToken "refreshToken1234"
    And accessToken with value "accessToken" exists for user 1234

  Scenario: Successful change of username
    When user requests change of username with value "kangaroo" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User successfully updated."
    And response contains key "status" and value "Created"
    And response status code is 201
    When user requests login with username "kangaroo" and password "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126"
    Then response status code is 200

  Scenario: Successful change of password
    When user requests change of password with value "96C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A012A" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User successfully updated."
    And response contains key "status" and value "Created"
    And response status code is 201
    When user requests login with username "user" and password "96C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A012A"
    Then response status code is 200

  Scenario: Failed change of username; new username already assigned
    Given user with userId 1235 exists and has username "kangaroo", password "secure_password"
    When user requests change of username with value "kangaroo" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User could not get updated. Username already taken."
    And response status code is 409
    And response contains key "status" and value "Conflict"

  Scenario: Failed change of password; new password is not valid
    When user requests change of password with value "Baum1234-2" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User could not get updated. Password needs to be a valid SHA-256 hash."
    And response status code is 409
    And response contains key "status" and value "Conflict"

  Scenario: Failed change of user. No Changes
    When user requests change of password with no changes, userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User could not get updated. No changes were made."
    And response contains key "status" and value "Conflict"
    And response status code is 409

  Scenario: RefreshToken of user is different after password change.
    When user requests change of password with value "96C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A012A" userId 1234 and accessToken "accessToken"
    Then response contains key "message" and value "User successfully updated."
    And response contains key "status" and value "Created"
    And response status code is 201
    When user requests login with username "user" and password "96C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A012A"
    And response contains key "tokenValue" and a different value than "refreshToken1234"
    Then response status code is 200

Feature: Find User with Username
  As a user
  I want to find another user

  Background:
    Given database is empty
    And user with userId 1234 exists and has username "kangaroo", password "pig-system"
    And accessToken with value "accessToken1" exists for user 1234
    And user with userId 1235 exists and has username "penguin", password "i-love-capitalism"
    And accessToken with value "accessToken2" exists for user 1235

  Scenario: Successful find another user
    When user with accessToken "accessToken1" searches user with search-value "penguin"
    Then response status code is 200
    And response contains the user with userId 1235

  Scenario: Successful find another user with username in wrong case
    When user with accessToken "accessToken1" searches user with search-value "PeNgUiN"
    Then response status code is 200
    And response contains the user with userId 1235

  Scenario: Successful find another user with username including some spaces
    When user with accessToken "accessToken1" searches user with search-value " pen guin "
    Then response status code is 200
    And response contains the user with userId 1235

  Scenario: Failed to find another user because username has spelling errors
    When user with accessToken "accessToken1" searches user with search-value "benguin"
    Then response status code is 404
    And response contains key "message" and value "User not found. Username was benguin"
    And response contains key "status" and value "Not Found"

  #kinda same but still
  Scenario: Failed to find another user because username does not exist
    When user with accessToken "accessToken1" searches user with search-value "bielefeld"
    Then response status code is 404
    And response contains key "message" and value "User not found. Username was bielefeld"
    And response contains key "status" and value "Not Found"
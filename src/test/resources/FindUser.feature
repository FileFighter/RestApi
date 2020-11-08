Feature:
  As a user
  I want to find another user

  Background:
    Given database is empty
    And user with id 1234 exists and has username "kangaroo", password "pig-system" and refreshToken "token1"
    And user with id 1235 exists and has username "penguin", password "i-love-capitalism" and refreshToken "token2"
    
  Scenario: Successful find another user
    When user with id "1234" and token "token1" searches user with search-value "penguin"
    Then response status code is 200
    And response contains the user with id 1235

  Scenario: Successful find another user with username in wrong case
    When user with id "1234" and token "token1" searches user with search-value "PeNgUiN"
    Then response status code is 200
    And response contains the user with id 1235
    
  Scenario: Failed to find another user because username has spelling errors
    When user with id "1234" and token "token1" searches user with search-value "benguin"
    Then response status code is 404
    And response contains key "message" and value "User with username 'benguin' does not exist"

  #kinds same but still
  Scenario: Failed to find another user because username does not exist
    When user with id "1234" and token "token1" searches user with search-value "bielefeld"
    Then response status code is 404
    And response contains key "message" and value "User with username 'bielefeld' does not exist"